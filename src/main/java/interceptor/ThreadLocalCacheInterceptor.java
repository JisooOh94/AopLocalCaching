package interceptor;

import java.lang.annotation.Annotation;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cache.CacheKey;
import cache.CacheStorage;
import cache.LocalCacheable;
import cache.impl.MapCacheStorage;
import cache.type.LocalCacheType;
import util.StringUtil;

@Aspect
public class ThreadLocalCacheInterceptor {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ThreadLocal<EnumMap<LocalCacheType,  CacheStorage>> threadLocalCache = new InheritableThreadLocal<>();

	@Pointcut("@annotation(cache.LocalCacheable)")
	public void annotion(){}

	@SuppressWarnings("unchecked")
	@Around("annotion() && @annotation(target)")
	public <T> T methodCall(ProceedingJoinPoint invoker, LocalCacheable target) throws Throwable {
		EnumMap<LocalCacheType, CacheStorage> cacheStorageCollection = threadLocalCache.get();
		if(cacheStorageCollection == null) {
			cacheStorageCollection = new EnumMap<>(LocalCacheType.class);
			threadLocalCache.set(cacheStorageCollection);
		}

		CacheStorage<T> cacheStorage = cacheStorageCollection.get(target.type());
		if(cacheStorage == null) {
			cacheStorage = new MapCacheStorage<>(target.maxSize());
			cacheStorageCollection.put(target.type(), cacheStorage);
		}

		String key = generateCacheKey(target.keyFormat(), target.keyPrefix(), invoker.getArgs(), ((MethodSignature)invoker.getSignature()).getMethod().getParameterAnnotations());

		T cachedValue = cacheStorage.getCache(key);
		if(cachedValue == null) {
			logger.info("# get from DB. key : {}", key);
			cachedValue = (T)invoker.proceed();
			cacheStorage.setCache(key, cachedValue);
		} else {
			logger.info("# get from cache. key : {}", key);
		}
		return cachedValue;
	}

	/**
	 * 캐싱 키 생성
	 * @param keyForamt 키 포맷
	 * @param keyPrefix 키 접두사
	 * @param args 메서드 파라미터 리스트
	 * @param annotations 메서드 파라미터에 적용되어있는 어노테이션 리스트
	 * @return 캐시키
	 */
	private String generateCacheKey(String keyForamt, String keyPrefix, Object[] args, Annotation[][] annotations) {
		//메서드 파라미터중, @CacheKey 어노테이션이 적용되어있는 파라미터만 키에 포함시킨다.
		List<Object> keyParamList = IntStream.range(0, args.length)
				.boxed()
				.filter(idx -> annotations[idx] != null)
				.filter(idx -> Stream.of(annotations[idx]).anyMatch(annotation -> annotation.annotationType() == CacheKey.class))
				.map(idx -> args[idx])
				.collect(Collectors.toList());

		//@CacheKey 어노테이션이 적용된 파라미터가 없다면, 전체 파라미터를 키에 포함시킨다.
		return StringUtil.format(keyForamt, keyPrefix, keyParamList.isEmpty() ? args : keyParamList.toArray());
	}
}