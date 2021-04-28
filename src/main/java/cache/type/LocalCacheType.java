package cache.type;

import cache.CacheStorage;

public enum LocalCacheType {
	COMMON_CACHE(CacheStorage.INF),
	USER_INFO_CACHE(100)
	;

	private int defaultSize;

	LocalCacheType(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	public int getDefaultSize() {
		return defaultSize;
	}
}
