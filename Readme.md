# LocalCachingModule
# AS-IS
* ThreadLocal 을 멤버필드로 가지는 ThreadLocalCacheSupport 클래스 정의
```java
public abstract class ThreadLocalCacheSupport<T> {
	private ThreadLocal<Map<String, T>> threadLocal = new InheritableThreadLocal<Map<String, T>>();
	
	protected T getCacheValue(String key) { ... }
	
	protected void putCacheValue(String key, T value) { ... }
	
	protected void removeCache(String key) { ... }
}
```
* 캐시 토픽별로 ThreadLocalCacheSupport을 상속받는 Repository 클래스 정의
```java
public class PublicGroupRepository extends ThreadLocalCacheSupport<Object> {
	public PublicGroupInfo getPublicGroupInfo(String ownerId) {
		String key = getPublicGroupInfoKey(ownerId);
		PublicGroupInfo publicGroupInfo = (PublicGroupInfo)getCacheValue(key);
		if(publicGroupInfo == null) {
			try {
				publicGroupInfo = shareInvoker.getPublicGroupInfo(ownerId);
				putCacheValue(key, publicGroupInfo);
			} catch (ShareInvokeException e) {
				putCacheValue(key, DEFAULT_PUBLIC_GROUP_INFO);
			}
		}
		return publicGroupInfo;
	}
	...
}

public class ShareFolderRepository extends ThreadLocalCacheSupport<String> {
	public String getWorksGroupFolderName(String clientId, String ownerId) {
    	String groupFolderName;
    	try {
    		groupFolderName = getCacheValue(ownerId);
    		if (groupFolderName == null) {
    			GroupFolderInfo worksInfo = worksDmsHttpInvoker.getGroupFolderInfoDetail(clientId, ownerId);
    			groupFolderName = StringUtils.defaultIfBlank(worksInfo.getWorksName(), StringUtils.EMPTY);
    			putCacheValue(ownerId, groupFolderName);
    		}
    	} catch (Exception e) {
    		groupFolderName = StringUtils.EMPTY;
    		putCacheValue(ownerId, groupFolderName);
    	}
    	return groupFolderName;
    }
    ...
}
```

### AS-IS 의 문제점
* LocalCaching 사용 위해 ThreadLocalCacheSupport 를 상속받는 별도의 Repository 클래스 정의 필요
* putCache, getCache 호출 코드의 중복
* Cache topic 별로 개별적으로 ThreadLocal 객체를 가지게되어 메모리 낭비
* Cache topic 별 정의한 Cache 모듈 객체들을 threadLocalRepositoryList 에 등록하여 ThreadLocalClearInterceptor에서 요청 완료후 clear 하도록 처리 필요, 관리에 어려움 (Human error발생 가능)
```java
<beans profile="NCS">
	<util:list id="threadLocalRepositoryList">
    	<ref bean="nbaseRepository"/>
    	<ref bean="compReqInfoRepository"/>
    	<ref bean="shareFolderRepository"/>
        <ref bean="shareLogDataRepository"/>
        <ref bean="blockExtensionRepository"/>
        <ref bean="userDirectoryManager"/>
        <ref bean="publicGroupRepository"/>
	</util:list>
</beans>

<mvc:interceptors>
        <bean class="com.naver.ndrive.compress.service.interceptor.ThreadLocalClearInterceptor">
            <property name="supportList" ref="threadLocalRepositoryList"/>
        </bean>
</mvc:interceptors>

public class ThreadLocalClearInterceptor implements HandlerInterceptor {
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		if (CollectionUtils.isNotEmpty(supportList)) {
			for (ThreadLocalCacheSupport<?> support : supportList) {
				support.clearCache();
			}
		}
	}
}
```

# TO-BE
* LocalCaching 로직을 횡단관심사 모듈로 분리
```java
@Aspect
public class LocalCacheSupport {
	private final ThreadLocal<EnumMap<LocalCacheTopic, CacheStorage>> threadLocalCache = new InheritableThreadLocal<>();
	
	@SuppressWarnings("unchecked")
	@Pointcut("@annotation(target) && execution(* *(..))")
	public void annotion(LocalCacheable target) {}
	
	@SuppressWarnings("unchecked")
	@Around("annotion(target)")
	public <T> T methodCall(ProceedingJoinPoint invoker, LocalCacheable target) throws Throwable {
		//caching logic
	}
}
```

* 어노테이션을 통한 AOP 로 캐싱 적용
```java
@LocalCacheable
public PublicGroupInfo getPublicGroupInfo(String ownerId) {
	return shareInvoker.getPublicGroupInfo(ownerId);
}

@LocalCacheable
public String getWorksGroupFolderName(String clientId, String ownerId) {
	try{
		GroupFolderInfo worksInfo = worksDmsHttpInvoker.getGroupFolderInfoDetail(clientId, ownerId)
		return StringUtils.defaultIfBlank(worksInfo.getWorksName(), StringUtils.EMPTY);
	} catch (Exception e) {
		return StringUtils.EMPTY;
	}
}
```
* 하나의 ThreadLocal 객체에서 모든 캐시데이터 관리
> AS-IS

![image](https://user-images.githubusercontent.com/48702893/121021984-e560f000-c7dc-11eb-8446-413d3d43dce0.png)

> TO-BE

![image](https://user-images.githubusercontent.com/48702893/121022249-20632380-c7dd-11eb-8fd5-c6b4da840327.png)

* 2중 해싱으로 인한 성능 저하를 완화하기위해 Topic 을 Enum 으로 정의 후, Topic 맵을 EnumMap 으로 선언

```java
//getCache
EnumMap<LocalCacheTopic, CacheStorage> cacheMapCollection = threadLocalCache.get();
Map<Key, Value> cacheMap = cacheMapCollection.get(topic);
return cacheMap.get(key);
```

### TO-BE 의 문제점
1. AOP 프록시 객체로 전달되는 파라미터 중 cache key 로 사용할 파라미터를 알 수 없음
```java
public class ShareFolderRepository extends ThreadLocalCacheSupport<String> {
	public String getWorksGroupFolderName(String clientId, String ownerId) {
    	String groupFolderName;
    	try {
    		groupFolderName = getCacheValue(ownerId);
    		...
    	}
    }
    ...
}
```
```java

@LocalCacheable
public String getWorksGroupFolderName(String clientId, String ownerId) {
	...
}
```
* 선택적 CacheKey 적용을 위해 @CacheKey 파라미터 어노테이션 추가
```java
@LocalCacheable
public String getWorksGroupFolderName(String clientId, @CacheKey String ownerId) {
	...
}

//LocalCacheSupport
private String generateCacheKey(Object[] args, Annotation[][] annotations) {
	//메서드 파라미터중, @CacheKey 어노테이션이 적용되어있는 파라미터만 키에 포함시킨다.
	List<Object> keyParamList = IntStream.range(0, args.length)
			.boxed()
			.filter(idx -> annotations[idx] != null)
			.filter(idx -> Stream.of(annotations[idx]).anyMatch(annotation -> annotation.annotationType() == CacheKey.class))
			.map(idx -> args[idx])
			.collect(Collectors.toList());
	
	...
}
```

2.public 메서드에 대해서만 캐싱 적용 가능하여 여전히 별도의 Repository 클래스 필요
```java
//PublicGroupRepository.class
@LocalCacheable
public PublicGroupInfo getPublicGroupInfo(String ownerId) {
	return shareInvoker.getPublicGroupInfo(ownerId);
}

//WmLogAppender.class
@Autowired
private PublicGroupRepository publicGroupRepository;

private String getPublicGroupHistoryLog(String ownerId) {
	...
	PublicGroupInfo publicGroupInfo = publicGroupRepository.getPublicGroupInfo(ownerId);  
	int domainId = publicGroupInfo.getDomainId();
	int tenantId = publicGroupInfo.getTenantId();
	...
}
```
* Proxy 객체를 사용하는 Srping AOP 대신, AspectJ AOP 를 사용하여, Compile time weaving 을 통해 private method 에 localcahing 적용
```java
//WmLogAppender.class
@Autowired
private String getPublicGroupHistoryLog(String ownerId) {
	...
	PublicGroupInfo publicGroupInfo = getPublicGroupInfo(ownerId);  
	int domainId = publicGroupInfo.getDomainId();
	int tenantId = publicGroupInfo.getTenantId();
	...
}

@LocalCacheable
private PublicGroupInfo getPublicGroupInfo(String ownerId) {
	return shareInvoker.getPublicGroupInfo(ownerId);
}
```

3. 캐시데이터에 캐시 만료 시간 적용 불가능
```java
//BlockExtensionRepository.java
public List<String> get(final int domainId) {
	// local cache 조회
	Map<String, Object> cacheValue = getCacheValue(String.valueOf(domainId));
	if (MapUtils.isNotEmpty(cacheValue)) {
		Date curDate = new Date();
		Date createDate = (Date)cacheValue.get(KEY_CREATETIME);
		
		if (expireSeconds <= 0 || curDate.before(DateUtils.addSeconds(createDate, expireSeconds))) {
			return (List<String>)cacheValue.get(KEY_EXTENSIONS);
		}
	}
	...
}
```
* 어노테이션 파라미터로 만료시간 간단하게 설정할 수 있는 기능 제공
```java
@LocalCacheable(expireTime = 15000L)
private PublicGroupInfo getPublicGroupInfo(String ownerId) {
	return shareInvoker.getPublicGroupInfo(ownerId);
}
``` 

# 성능 테스트
