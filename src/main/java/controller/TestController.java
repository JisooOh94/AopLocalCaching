package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * @author jisoooh
 */
@Controller
@RequestMapping("/test")
public class TestController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final JdbcTemplate jdbcTemplate;

	private final String env;

	@Autowired
	public TestController(JdbcTemplate jdbcTemplate, @Value("${env}") String env) {
		this.jdbcTemplate = jdbcTemplate;
		this.env = env;
	}

	@GetMapping("/helloWorld")
	public String helloWorld() {
		return "index";
	}

	@GetMapping("/property/injection")
	public String propertyInjection() {
		System.out.println(env);
		return "index";
	}

	@GetMapping("/log4j2")
	public String loggingModule() {
		logger.debug("Test logging");
		return "index";
	}

	@GetMapping("/log4j2/logLevel")
	public void runtimeLogLevelChangeTest() {
		logger.error("error log");
		logger.warn("warn log");
		logger.info("info log");
		logger.debug("debug log");
	}

	@GetMapping("/embeddeddb")
	public void embeddedDbTest() {
		List<Map<String, Object>> userInfoList = jdbcTemplate.query("SELECT * FROM user_info", (row, rowIdx) -> {
			Map<String, Object> result = new HashMap<>();
			result.put("userId", row.getString("user_id"));
			result.put("userNo", row.getInt("user_no"));
			return result;
		});
		logger.info(userInfoList.toString());
	}
}
