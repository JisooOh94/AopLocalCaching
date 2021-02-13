package controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jisoooh
 */
@Controller
@RequestMapping("/test")
public class TestController {
	@Value("${env}")
	private String env;

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
