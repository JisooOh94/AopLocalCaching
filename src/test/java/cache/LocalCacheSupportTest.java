package cache;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.EnumMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import cache.impl.EnumMapCacheStorage;
import cache.impl.MapCacheStorage;
import cache.type.CommonCacheType;
import cache.type.LocalCacheType;

@RunWith(MockitoJUnitRunner.class)
public class LocalCacheSupportTest {
	private LocalCacheSupport localCacheSupport = new LocalCacheSupport();
	@Mock
	private ThreadLocal threadLocal;

	private int size = 100;
	private String sampleKey = "sampleKey";
	private CommonCacheType sampleCommonCacheKey = CommonCacheType.SAMPLE;
	private String sampleValue = "sampleValue";
	private LocalCacheType cacheType = LocalCacheType.USER_INFO_CACHE;
	private int cacheSize = 100;
	private EnumMap<LocalCacheType, CacheStorage> cacheStorageCollection = new EnumMap<>(LocalCacheType.class);

	@Before
	public void init() {
		ReflectionTestUtils.setField(localCacheSupport, "threadLocalCache", threadLocal);
	}

	@Test
	public void getCache() {
		CacheStorage<String, String> cacheStorage = new MapCacheStorage<>(size);
		cacheStorage.setCache(sampleKey, sampleValue);
		cacheStorageCollection.put(cacheType, cacheStorage);
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		String result = localCacheSupport.getCache(cacheType, sampleKey);
		assertEquals(sampleValue, result);
	}

	@Test
	public void getCache_cacheStorageCollection_null() {
		given(threadLocal.get()).willReturn(null);
		String result = localCacheSupport.getCache(cacheType, sampleKey);
		assertNull(result);
	}

	@Test
	public void getCache_cacheStorage_null() {
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		String result = localCacheSupport.getCache(cacheType, sampleKey);
		assertNull(result);
	}

	@Test
	public void getCache_commonCache() {
		CacheStorage<CommonCacheType, String> cacheStorage = new EnumMapCacheStorage<>(CommonCacheType.SAMPLE, CacheStorage.INF);
		cacheStorage.setCache(sampleCommonCacheKey, sampleValue);
		cacheStorageCollection.put(LocalCacheType.COMMON_CACHE, cacheStorage);
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		String result = localCacheSupport.getCache(LocalCacheType.COMMON_CACHE, sampleCommonCacheKey);
		assertEquals(sampleValue, result);
	}

	@Test
	public void setCache() {
		CacheStorage<String, String> cacheStorage = new MapCacheStorage<>(cacheSize);
		cacheStorageCollection.put(cacheType, cacheStorage);
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		localCacheSupport.setCache(cacheType, cacheSize, sampleKey, sampleValue);

		assertEquals(sampleValue, cacheStorage.getCache(sampleKey));
	}

	@Test
	public void setCache_cacheStorage_null() {
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		localCacheSupport.setCache(cacheType, cacheSize, sampleKey, sampleValue);

		CacheStorage<String, String> cacheStorage = cacheStorageCollection.get(cacheType);

		assertEquals(sampleValue, cacheStorage.getCache(sampleKey));
		assertEquals(cacheStorage.getMaxSize(), cacheSize);
	}

	@Test
	public void setCache_cacheStorage_full() {
		String sampleKey_2 = "sampleKey_2";
		CacheStorage<String, String> cacheStorage = new MapCacheStorage<>(1);
		cacheStorage.setCache(sampleKey, sampleValue);

		cacheStorageCollection.put(cacheType, cacheStorage);
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		localCacheSupport.setCache(cacheType, cacheSize, sampleKey_2, sampleValue);

		assertEquals(sampleValue, cacheStorage.getCache(sampleKey_2));
		assertNull(cacheStorage.getCache(sampleKey));
	}

	@Test
	public void setCache_commonCache() {
		CacheStorage<CommonCacheType, String> cacheStorage = new EnumMapCacheStorage<>(CommonCacheType.SAMPLE, CacheStorage.INF);
		cacheStorageCollection.put(LocalCacheType.COMMON_CACHE, cacheStorage);
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		localCacheSupport.setCache(CommonCacheType.SAMPLE, sampleValue);

		assertEquals(cacheStorage.getCache(CommonCacheType.SAMPLE), sampleValue);
	}

	@Test
	public void setCache_commonCache_cacheStorage_null() {
		given(threadLocal.get()).willReturn(cacheStorageCollection);

		localCacheSupport.setCache(CommonCacheType.SAMPLE, sampleValue);

		CacheStorage<CommonCacheType, String> cacheStorage = cacheStorageCollection.get(LocalCacheType.COMMON_CACHE);
		assertTrue(cacheStorage instanceof EnumMapCacheStorage);
		assertEquals(cacheStorage.getCache(CommonCacheType.SAMPLE), sampleValue);
	}
}
