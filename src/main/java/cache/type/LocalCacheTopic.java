package cache.type;

import static cache.CacheStorage.*;

public enum LocalCacheTopic {
	//map cache
	USER_INFO_CACHE(100),
	SINGLETON_CACHE(INF),

	//singleton cache
	COMMON_CACHE();

	private int defaultSize;

	LocalCacheTopic() {
	}

	LocalCacheTopic(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	public int getDefaultSize() {
		return defaultSize;
	}
}
