package interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cache.LocalCacheable;
import cache.LocalCacheSupport;

@Aspect
public class ThreadLocalCacheInterceptor {
	private LocalCacheSupport localCacheSupport;

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