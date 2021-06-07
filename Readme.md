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
* LocalCaching 로직을 횡단관심사 모듈로 분리후 어노테이션을 통한 AOP 로 적용
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
//TODO : 그림
 
* 2중 해싱으로 인한 성능 저하를 완화하기위해 Topic 을 Enum 으로 정의 후, Topic 맵을 EnumMap 으로 선언

* 선택적 CacheKey 적용을 위해 @CacheKey 파라미터 어노테이션 추가
```java
@LocalCacheable
public String getWorksGroupFolderName(String clientId, @CacheKey String ownerId) {
	...
}
```

### AspectJ AOP

 

