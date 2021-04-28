package cache.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cache.CacheStorage;

public class MapCacheStorage<K, V> implements CacheStorage<K, V> {
	protected Map<K, V> storage;
	protected int maxSize;

	public MapCacheStorage() {
	}

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

	@Override
	public int getMaxSize() {
		return maxSize;
	}
}
