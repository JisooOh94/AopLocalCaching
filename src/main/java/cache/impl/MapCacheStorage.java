package cache.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cache.CacheStorage;

public class MapCacheStorage<T> implements CacheStorage<T> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String, T> storage;
	private int maxSize;

	public MapCacheStorage(int size) {
		this.storage = new HashMap<>();
		this.maxSize = size;
	}

	@Override
	public T getCache(String key) {
		return storage.get(key);
	}

	@Override
	public boolean setCache(String key, T value) {
		logger.info("# setCache. key : {}, value : {}", key, value);
		if (!storage.containsKey(key) && storage.size() == maxSize) {
			logger.info("# Cache is full. Delete element");
			storage.entrySet().iterator().remove();
		}
		storage.put(key, value);
		return true;
	}
}
