package cache.impl;

import java.util.HashMap;
import java.util.Map;

import cache.CacheStorage;

public class CommonCacheStorage<CommonCacheType, T> implements CacheStorage<CommonCacheType, T> {
	private Map<CommonCacheType, T> storage;

	public CommonCacheStorage() {
		this.storage = new HashMap<>();
	}

	@Override
	public T getCache(CommonCacheType key) {
		return storage.get(key);
	}

	@Override
	public boolean setCache(CommonCacheType key, T value) {
		storage.put(key, value);
		return true;
	}
}
