package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import controller.TestController;

@Configuration
@PropertySource("classpath:config.properties")
public class ServletContextConfig {
	private final Environment environment;

	@Autowired
	public ServletContextConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public TestController testController() {
		return new TestController(environment.getProperty("env"));
	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		String prefix = environment.getProperty("view.prefix");
		String suffix = environment.getProperty("view.suffix");
		return new InternalResourceViewResolver(prefix, suffix);
	}
}
