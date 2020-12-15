package controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author jisoooh
 */
@RequestMapping("/test")
public class TestController {

	@GetMapping("/helloWorld")
	public String helloWorld() {
		return "index";
	}
}
