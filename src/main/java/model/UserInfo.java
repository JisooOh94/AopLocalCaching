package model;

/**
 * @author jisoooh
 */
public class UserInfo {
	private int userNo;
	private String userId;
	private int userAge;
	private String userName;

	public UserInfo(int userNo, String userId, int userAge, String userName) {
		this.userNo = userNo;
		this.userId = userId;
		this.userAge = userAge;
		this.userName = userName;
	}

	public int getUserNo() {
		return userNo;
	}

	public String getUserId() {
		return userId;
	}

	public int getUserAge() {
		return userAge;
	}

	public String getUserName() {
		return userName;
	}
}
