package cache.impl;

import java.util.EnumMap;

public class EnumMapCacheStorage<K, V> extends MapCacheStorage<K, V> {
	@SuppressWarnings("unchecked")
	public EnumMapCacheStorage(K type, int size) {
		super();
		this.storage = new EnumMap<>(((Enum)type).getDeclaringClass());
		this.maxSize = size;
	}
}