package interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import cache.LocalCacheSupport;
import cache.LocalCacheable;

@Aspect
public class ThreadLocalCacheInterceptor {
	private final LocalCacheSupport localCacheSupport;

	public ThreadLocalCacheInterceptor(LocalCacheSupport localCacheSupport) {
		this.localCacheSupport = localCacheSupport;
	}

	@Pointcut("@annotation(cache.LocalCacheable)")
	public void annotion() {
	}

	@SuppressWarnings("unchecked")
	@Around("annotion() && @annotation(target)")
	public <T> T methodCall(ProceedingJoinPoint invoker, LocalCacheable target) throws Throwable {
		String key = localCacheSupport.generateCacheKey(target.keyFormat(), target.keyPrefix(), invoker.getArgs(), ((MethodSignature) invoker.getSignature()).getMethod().getParameterAnnotations());

		T cachedValue = localCacheSupport.getCache(target.type(), key);
		if (cachedValue == null) {
			cachedValue = (T) invoker.proceed();
			localCacheSupport.setCache(target.type(), target.maxSize(), key, cachedValue);
		}

		return cachedValue;
	}

}