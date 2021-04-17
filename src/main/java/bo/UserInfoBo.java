package bo;

import static cache.type.LocalCacheType.*;

import cache.CacheKey;
import cache.LocalCacheable;
import dao.UserInfoDao;
import model.UserInfo;

public class UserInfoBo {
	private final UserInfoDao userInfoDao;

	public UserInfoBo(UserInfoDao userInfoDao) {
		this.userInfoDao = userInfoDao;
	}

	@LocalCacheable(type = USER_INFO_CACHE, maxSize = 10, keyFormat = "{}-{}", keyPrefix = "userInfo")
	public UserInfo getUserInfo(@CacheKey String userId) {
		return userInfoDao.getUserInfo(userId);
	}

	@LocalCacheable(type = USER_INFO_CACHE, maxSize = 10, keyFormat = "{}-{}", keyPrefix = "userName")
	public String getUserName(String userId) {
		return userInfoDao.getUserName(userId);
	}

	@LocalCacheable(type = USER_INFO_CACHE, maxSize = 10, keyFormat = "{}-{}", keyPrefix = "userAge")
	public int getUserAge(String userId) {
		return userInfoDao.getUserAge(userId);
	}
}