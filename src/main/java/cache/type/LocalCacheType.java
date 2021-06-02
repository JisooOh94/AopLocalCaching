package cache.type;

import static cache.CacheStorage.*;

public enum LocalCacheType {
	//map cache
	USER_INFO_CACHE(100),
	SINGLETON_CACHE(INF),

	//singleton cache
	COMMON_CACHE();

	private int defaultSize;

	LocalCacheType() {
	}

	LocalCacheType(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	public int getDefaultSize() {
		return defaultSize;
	}
}
