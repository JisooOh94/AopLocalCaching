package cache;

public interface CacheStorage<K, V> {
	V getCache(K key);

	boolean setCache(K key, V value);

	int getMaxSize();
}
