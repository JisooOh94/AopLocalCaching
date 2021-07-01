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
public class UserInfoRepository extends ThreadLocalCacheSupport<Object> {
	public PublicGroupInfo getUserInfo(String userId) {
		String key = getUserInfoKey(userId);
		UserInfo userInfo = (UserInfo)getCacheValue(key);
		if(userInfo == null) {
			try {
				userInfo = userInvoker.getUserInfo(userId);
				putCacheValue(key, userInfo);
			} catch (UserInvokeException e) {
				putCacheValue(key, DEFAULT_USER_INFO);
			}
		}
		return userInfo;
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
    	<ref bean="userInfoRepository"/>
	</util:list>
</beans>

<mvc:interceptors>
        <bean class="ThreadLocalClearInterceptor">
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
public UserInfo getUserInfo(String userId) {
	return userInvoker.getUserInfo(userId);
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
public class UserInfoRepository extends ThreadLocalCacheSupport<String> {
	public String getUserDetailedInfo(String userId, String userNo) {
    	String userName;
    	try {
    		userName = getCacheValue(userId);
    		...
    	}
    }
    ...
}
```
```java

@LocalCacheable
public String getUserDetailedInfo(String userId, String userNo) {
	...
}
```
* 선택적 CacheKey 적용을 위해 @CacheKey 파라미터 어노테이션 추가
```java
@LocalCacheable
public String getUserDetailedInfo(String userId, @CacheKey String userNo) {
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
//UserInfoRepository.class
@LocalCacheable
public UserInfo getUserInfo(String userId) {
	return userInvoker.getUserInfo(userId);
}

//UserInfoBo.class
@Autowired
private UserInfoRepository userInfoRepository;

private String getUserInfo(String userId) {
	...
	UserInfo userInfo = userInfoRepository.getUserInfo(userId);  
	String userName = userInfo.getUserName();
	int userAge = userInfo.getUserAge();
	...
}
```
* Proxy 객체를 사용하는 Srping AOP 대신, AspectJ AOP 를 사용하여, Compile time weaving 을 통해 private method 에 localcahing 적용
```java
//UserInfoBo.class
@Autowired
private String getUserInfo(String ownerId) {
	...
	UserInfo userInfo = getUserInfo(userId);  
   	String userName = userInfo.getUserName();
   	int userAge = userInfo.getUserAge();
	...
}

@LocalCacheable
private UserInfo getUserInfo(String userId) {
	return userInvoker.getUserInfo(userId);
}
```

3. 캐시데이터에 캐시 만료 시간 적용 불가능
```java
public List<String> get(final int userId) {
	// local cache 조회
	Map<String, Object> cacheValue = getCacheValue(String.valueOf(userId));
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
* 어노테이션 파라미터로 만료시간 설정할 수 있는 기능 제공
```java
@LocalCacheable(expireTime = 15000L)
private UserInfo getUserInfo(String userId) {
	return userInvoker.getUserInfo(userId);
}
``` 

<br>

# 성능 테스트
### 테스트 표본
* Caching 클래스를 이용한 캐싱
* AspectJ AOP 를 이용한 캐싱

### 테스트 설계
* max cache size : 100
* case 1 : Cache Hit 만 발생
    * getCache 만 수행했을때의 성능 비교
* case 2 : Cache Miss 만 발생
    * getCache + setCache 수행했을때의 성능 비교

### 테스트 결과 - 1
| 시나리오 | 캐시 적중률(%) | 캐싱 종류 | 평균 수행 시간(ms) | 비고 |
|:----------:|:------------:|:-----------------:|:--------------:|:-----:|
|1|100|aop|10.08||
| | |direct|1.98||
|2|0|aop|11.04||
| | |direct|2.69||

### 테스트 결과 분석 - 1
* Aop 를 이용한 캐시가 Direct 캐시에 비해 약 5배 정도 성능이 떨어짐
* Aop 캐시 로직의 각 단계별로 시간 측정 결과
    * key 생성 : 0.0785
    * threadLocal에서 cache 조회 : 0.0142
    * threadLocal 에 cache 설정 : 0.0181
* key 생성 로직에서 많은 부하 발생
    * 메서드의 전체 파라미터 탐색
    * stream 을 이용한 loop
    * reflection 을 통한 Method signature 및 파라미터 조회
```java
//메서드 파라미터중, @CacheKey 어노테이션이 적용되어있는 파라미터만 키에 포함시킨다.
List<Object> keyParamList = IntStream.range(0, args.length)
		.boxed()
		.filter(idx -> annotations[idx] != null)
		.filter(idx -> Stream.of(annotations[idx]).anyMatch(annotation -> annotation.annotationType() == CacheKey.class))
		.map(idx -> args[idx])
		.collect(Collectors.toList());

//@CacheKey 어노테이션이 적용된 파라미터가 없다면, 전체 파라미터를 키에 포함시킨다.
return StringUtil.format(keyForamt, keyParamList.isEmpty() ? args : keyParamList.toArray());
```

* key 생성 로직에서 stream 이 아닌, for-each 문으로 loop 하도록 수정
```java
for(int idx = 0; idx < args.length; idx++) {
	if(annotations[idx] != null) {
		for(Annotation annotation : annotations[idx]) {
			if(annotation.annotationType() == CacheKey.class) {
				keyParamList.add(args[idx]);
				break;
			}
		}
	}
}
```

### 테스트 결과 - 2


| 시나리오 | 캐시 적중률(%) | 캐싱 종류 | 평균 수행 시간(ms) | 비고 |
|:----------:|:------------:|:-----------------:|:--------------:|:-----:|
|1|100|aop|3.75||
| | |direct|1.95||
|2|0|aop|5.28||
| | |direct|2.49||


### 테스트 결과 분석 - 2
* 성능 측정결과 Aop 캐시의 성능이 2배 이상 향상
* Aop 캐시 로직의 각 단계별로 시간 측정 결과 key 생성 로직의 소요시간이 2배 이상 줄어듬
    * key 생성 : 0.0316
    * threadLocal에서 cache 조회 : 0.0162
    * threadLocal 에 cache 설정 : 0.0226

### 테스트 결과 정리
* Aop Cache 는 Direct Cache 에 비해 약 2배정도의 시간 소모
    * 컴파일 타임 위빙으로 프록시 객체로 인한 성능저하는 없음
    * 1번의 해싱만 하면 되는 Direct Cache 에 비해 Aop Cache 는 2번의 Hashing 이 필요하여 약간의 성능 저하 발생
        > Topic 구분용 Map 을 EnumMap 으로 사용하여 2번의 Hashing 으로 인한 성능저하는 거의 없을것으로 예상
    * Cache Key 생성 과정에서 메서드 전체 파라미터 탐색이 수행되어 결정적인 성능 저하 발생
* Cache Key 생성 로직을 수정하여 추가적은 성능 개선 가능
* Aop Cache 를 통해 얻는 이득(중복코드 제거, 사용의 편리, 개발 시간 절약) 과 손실(성능 저하) 을 따져본뒤 적용 필요