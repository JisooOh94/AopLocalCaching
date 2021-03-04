package bo;

import java.util.List;

import dao.UserInfoDao;
import model.UserInfo;

public class UserInfoBo {
	private final UserInfoDao userInfoDao;

	public UserInfoBo(UserInfoDao userInfoDao) {
		this.userInfoDao = userInfoDao;
	}

	public List<UserInfo> getUserInfo(String userId) {
		return userInfoDao.getUserInfo(userId);
	}
}
