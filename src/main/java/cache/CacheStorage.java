package cache;

public interface CacheStorage<T> {
	T getCache(String key);
	boolean setCache(String key, T value);
}
