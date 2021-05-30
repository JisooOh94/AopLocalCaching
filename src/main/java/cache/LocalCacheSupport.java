package cache;

import static cache.type.LocalCacheType.*;
import static org.apache.commons.lang3.ObjectUtils.*;

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
import cache.impl.EnumMapCacheStorage;
import cache.impl.MapCacheStorage;
import cache.type.LocalCacheType;
import util.StringUtil;

@Aspect
public class LocalCacheSupport {
	private final ThreadLocal<EnumMap<LocalCacheType, CacheStorage>> threadLocalCache = new InheritableThreadLocal<>();

	@Pointcut("@annotation(target) && execution(* *(..))")
	public void annotion(LocalCacheable target) {
	}

	@SuppressWarnings("unchecked")
	@Around("annotion(target)")
	public <T> T methodCall(ProceedingJoinPoint invoker, LocalCacheable target) throws Throwable {
		String key = generateCacheKey(target.keyFormat(), target.keyPrefix(), invoker.getArgs(), ((MethodSignature) invoker.getSignature()).getMethod().getParameterAnnotations());

		T cachedValue = getCache(target.type(), key);
		if (cachedValue == null) {
			cachedValue = (T) invoker.proceed();
			setCache(target.type(), target.maxSize(), key, cachedValue);
		}

		return cachedValue;
	}

	public <V> V getCache(LocalCacheType key) {
		return getCache(SINGLETON_CACHE, key);
	}

	@SuppressWarnings("unchecked")
	public <K, V> V getCache(LocalCacheType type, K key) {
		EnumMap<LocalCacheType, CacheStorage> cacheStorageCollection = threadLocalCache.get();
		if(cacheStorageCollection == null) return null;

		CacheStorage<K, V> cacheStorage = cacheStorageCollection.get(type);
		if(cacheStorage == null) return null;

		return cacheStorage.getCache(key);
	}

	public <V> void setCache(LocalCacheType key, V val) {
		setCache(SINGLETON_CACHE, null, key, val);
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCache(LocalCacheType type, Integer size, K key, V val) {
		EnumMap<LocalCacheType, CacheStorage> cacheStorageCollection = getCacheStorageCollection();

		CacheStorage<K, V> cacheStorage = cacheStorageCollection.get(type);

		if (cacheStorage == null) {
			int cacheSize = defaultIfNull(size, type.getDefaultSize());
			cacheStorage = type == SINGLETON_CACHE ? new EnumMapCacheStorage<>(key, cacheSize) : new MapCacheStorage<>(cacheSize);
			cacheStorageCollection.put(type, cacheStorage);
		}

		cacheStorage.setCache(key, val);
	}

	/**
	 *
	 * @return
	 */
	private EnumMap<LocalCacheType, CacheStorage> getCacheStorageCollection() {
		EnumMap<LocalCacheType, CacheStorage> cacheStorageCollection = threadLocalCache.get();
		if (cacheStorageCollection == null) {
			cacheStorageCollection = new EnumMap<>(LocalCacheType.class);
			threadLocalCache.set(cacheStorageCollection);
		}
		return cacheStorageCollection;
	}

	/**
	 * 캐싱 키 생성
	 *
	 * @param keyForamt   키 포맷
	 * @param keyPrefix   키 접두사
	 * @param args        메서드 파라미터 리스트
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