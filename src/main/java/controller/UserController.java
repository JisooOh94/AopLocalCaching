package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import bo.UserInfoBo;
import model.UserInfo;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserInfoBo userInfoBo;

	@Autowired
	public UserController(UserInfoBo userInfoBo) {
		this.userInfoBo = userInfoBo;
	}

	@GetMapping("/info")
	public String getUserInfo(@RequestParam String userId, ModelMap model) {
		List<UserInfo> userInfoList = userInfoBo.getUserInfo(userId);
		model.addAttribute("userInfoList", userInfoList);
		return "userInfo";
	}
}
