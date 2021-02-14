package controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jisoooh
 */
@RequestMapping("/test")
public class TestController {
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
}
