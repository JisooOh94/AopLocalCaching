package cache.impl;

import java.util.HashMap;
import java.util.Map;

import cache.CacheStorage;

public class MapCacheStorage<K, V> implements CacheStorage<K, V> {
	private Map<K, V> storage;
	private int maxSize;

	public MapCacheStorage(int size) {
		this.storage = new HashMap<>();
		this.maxSize = size;
	}

	@Override
	public V getCache(K key) {
		return storage.get(key);
	}

	@Override
	public boolean setCache(K key, V value) {
		if (maxSize != INF && storage.size() == maxSize && !storage.containsKey(key)) {
			storage.entrySet().iterator().remove();
		}
		storage.put(key, value);
		return true;
	}
}
