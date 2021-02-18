package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jisoooh
 */
@RequestMapping("/test")
public class TestController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String env;

	public TestController(String env) {
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
}
