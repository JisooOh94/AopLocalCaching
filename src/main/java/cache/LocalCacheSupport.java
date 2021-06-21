package cache;

import static cache.type.LocalCacheTopic.*;

import java.lang.annotation.Annotation;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import cache.impl.EnumMapCacheStorage;
import cache.impl.MapCacheStorage;
import cache.type.LocalCacheTopic;
import util.StringUtil;

@Aspect
public class LocalCacheSupport {
	private final ThreadLocal<EnumMap<LocalCacheTopic, CacheStorage>> threadLocalCache = new InheritableThreadLocal<>();

	@SuppressWarnings("unchecked")
	@Pointcut("@annotation(target) && execution(* *(..))")
	public void annotion(LocalCacheable target) { }

	@SuppressWarnings("unchecked")
	@Around("annotion(target)")
	public <T> T methodCall(ProceedingJoinPoint invoker, LocalCacheable target) throws Throwable {
		String key = generateCacheKey(target.keyFormat(), invoker.getArgs(), ((MethodSignature) invoker.getSignature()).getMethod().getParameterAnnotations());

		T cachedValue = target.expireTime() != 0 ? getCache(target.topic(), key, target.expireTime()) : getCache(target.topic(), key);
		if (cachedValue == null) {
			cachedValue = (T) invoker.proceed();
			if(target.expireTime() != 0) {
				setCache(target.topic(), target.maxSize(), System.currentTimeMillis(), key, cachedValue);
			} else {
				setCache(target.topic(), target.maxSize(), key, cachedValue);
			}
		}

		return cachedValue;
	}

	public <V> V getCache(LocalCacheTopic key) {
		return getCache(SINGLETON_CACHE, key);
	}

	@SuppressWarnings("unchecked")
	public <K, V> V getCache(LocalCacheTopic topic, K key) {
		CacheStorage<K, V> cacheStorage = getCacheStorage(topic);

		return cacheStorage == null ? null : cacheStorage.getCache(key);
	}

	@SuppressWarnings("unchecked")
	public <K, V> V getCache(LocalCacheTopic topic, K key, long expireTime) {
		CacheStorage<K, Pair<V, Long>> cacheStorage = getCacheStorage(topic);
		if(cacheStorage == null) {
			return null;
		}

		Pair<V, Long> cachedValue = cacheStorage.getCache(key);
		return Calendar.getInstance().getTimeInMillis() - cachedValue.getRight() <= expireTime ? cachedValue.getLeft() : null;
	}

	private CacheStorage getCacheStorage(LocalCacheTopic topic) {
		EnumMap<LocalCacheTopic, CacheStorage> cacheStorageCollection = threadLocalCache.get();
		if (cacheStorageCollection == null) return null;

		CacheStorage cacheStorage = cacheStorageCollection.get(topic);
		if (cacheStorage == null) return null;

		return cacheStorage;
	}

	public <V> void setCache(LocalCacheTopic key, V val) {
		setCache(SINGLETON_CACHE, 0, 0, key, val);
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCache(LocalCacheTopic topic, int size, K key, V val) {
		CacheStorage cacheStorage = setCacheStorage(topic, key, size);
		cacheStorage.setCache(key, val);
	}

	@SuppressWarnings("unchecked")
	public <K, V> void setCache(LocalCacheTopic topic, int size, long creationTime, K key, V val) {
		CacheStorage<K, Pair<V, Long>> cacheStorage = setCacheStorage(topic, key, size);
		cacheStorage.setCache(key, Pair.of(val, creationTime));
	}

	private <K> CacheStorage setCacheStorage(LocalCacheTopic topic, K key, int size) {
		EnumMap<LocalCacheTopic, CacheStorage> cacheStorageCollection = threadLocalCache.get();
		if (cacheStorageCollection == null) {
			cacheStorageCollection = new EnumMap<>(LocalCacheTopic.class);
			threadLocalCache.set(cacheStorageCollection);
		}

		CacheStorage cacheStorage = cacheStorageCollection.get(topic);

		if (cacheStorage == null) {
			cacheStorage = topic == SINGLETON_CACHE ? new EnumMapCacheStorage<>(key, size) : new MapCacheStorage<>(size);
			cacheStorageCollection.put(topic, cacheStorage);
		}
		return cacheStorage;
	}

	/**
	 * 캐싱 키 생성
	 *
	 * @param keyForamt   키 포맷
	 * @param args        메서드 파라미터 리스트
	 * @param annotations 메서드 파라미터에 적용되어있는 어노테이션 리스트
	 * @return 캐시키
	 */
	private String generateCacheKey(String keyForamt, Object[] args, Annotation[][] annotations) {
		//메서드 파라미터중, @CacheKey 어노테이션이 적용되어있는 파라미터만 키에 포함시킨다.
		List<Object> keyParamList = IntStream.range(0, args.length)
				.boxed()
				.filter(idx -> annotations[idx] != null)
				.filter(idx -> Stream.of(annotations[idx]).anyMatch(annotation -> annotation.annotationType() == CacheKey.class))
				.map(idx -> args[idx])
				.collect(Collectors.toList());

		//@CacheKey 어노테이션이 적용된 파라미터가 없다면, 전체 파라미터를 키에 포함시킨다.
		return StringUtil.format(keyForamt, keyParamList.isEmpty() ? args : keyParamList.toArray());
	}
}