package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import controller.TestController;

@Configuration
@PropertySource("classpath:config.properties")
public class ServletContextConfig {
	private final Environment environment;
	private final JdbcTemplate jdbcTemplate;
	@Autowired
	public ServletContextConfig(Environment environment, JdbcTemplate jdbcTemplate) {
		this.environment = environment;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Bean
	public TestController testController() {
		return new TestController(jdbcTemplate, environment.getProperty("env"));
	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		String prefix = environment.getProperty("view.prefix");
		String suffix = environment.getProperty("view.suffix");
		return new InternalResourceViewResolver(prefix, suffix);
	}
}
