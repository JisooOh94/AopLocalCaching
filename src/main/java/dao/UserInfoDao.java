package dao;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import model.UserInfo;
import repository.Repository;

/**
 * @author jisoooh
 */
public class UserInfoDao {
	private static final String USER_NO = "user_no";
	private static final String USER_ID = "user_id";
	private static final String USER_AGE = "user_age";
	private static final String USER_NAME = "user_name";

	private final Repository repository;
	private DataSource dataSource;

	public UserInfoDao(Repository repository) {
		this.repository = repository;
	}

	public UserInfo getUserInfo(String userId) {
		System.out.println("# getUserInfo Called!");
		String query = "SELECT * FROM user_info WHERE user_id = :userId";

		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);

		return repository.selectOne(query, param, (row, idx) -> new UserInfo(row.getInt(USER_NO), row.getString(USER_ID), row.getInt(USER_AGE), row.getString(USER_NAME)));
	}

	public String getUserName(String userId) {
		String query = "SELECT user_name FROM user_info WHERE user_id = :userId";

		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);

		return repository.selectOne(query, param, String.class);
	}

	public int getUserAge(String userId) {
		String query = "SELECT user_age FROM user_info WHERE user_id = :userId";

		Map<String, Object> param = new HashMap<>();
		param.put("userId", userId);

		return repository.selectOne(query, param, Integer.class);
	}
}
