package cache;

import static cache.CacheStorage.*;
import static cache.type.LocalCacheTopic.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;
import cache.type.LocalCacheTopic;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LocalCacheable {
	LocalCacheTopic topic() default COMMON_CACHE;

	String keyFormat() default "{}";

	String keyPrefix() default StringUtils.EMPTY;

	int maxSize() default 0;

	long expireTime() default 0;
}