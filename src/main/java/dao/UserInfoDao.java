package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repository.Repository;
import model.UserInfo;

/**
 * @author jisoooh
 */
public class UserInfoDao {
	private static final String USER_NO = "user_no";
	private static final String USER_ID = "user_id";
	private static final String USER_AGE = "user_age";
	private static final String USER_NAME = "user_name";

	private final Repository repository;

	public UserInfoDao(Repository repository) {
		this.repository = repository;
	}

	public List<UserInfo> getUserInfo(String userId) {
		String query = "SELECT * FROM user_info WHERE user_id = :userId";

		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);

		return repository.select(query, param, (row, idx) -> new UserInfo(row.getInt(USER_NO), row.getString(USER_ID), row.getInt(USER_AGE), row.getString(USER_NAME)));
	}
}
