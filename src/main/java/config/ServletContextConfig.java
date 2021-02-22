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
	private static final String PROPERTY_VIEW_PREFIX_KEY = "view.prefix";
	private static final String PROPERTY_VIEW_SUFFIX_KEY = "view.suffix";
	private static final String PROPERTY_ENV_KEY = "env";
	private final Environment environment;

	@Autowired
	public ServletContextConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public TestController testController() {
		return new TestController(environment.getProperty(PROPERTY_ENV_KEY));
	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		String prefix = environment.getProperty(PROPERTY_VIEW_PREFIX_KEY);
		String suffix = environment.getProperty(PROPERTY_VIEW_SUFFIX_KEY);
		return new InternalResourceViewResolver(prefix, suffix);
	}
}
