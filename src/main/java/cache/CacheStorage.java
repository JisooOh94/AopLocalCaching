package cache;

public interface CacheStorage<K, V> {
	int INF = -1;
	int DEFAULT = 0;
	V getCache(K key);
	boolean setCache(K key, V value);
}
