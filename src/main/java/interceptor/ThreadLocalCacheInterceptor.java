package interceptor;

import java.util.EnumMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

		String key = StringUtil.format(target.keyFormat(), target.keyPrefix(), invoker.getArgs());

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
}
