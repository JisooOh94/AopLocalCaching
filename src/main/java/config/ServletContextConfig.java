package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import controller.TestController;

@Configuration
@PropertySource("classpath:config.properties")
@ComponentScan(basePackageClasses = TestController.class)
public class ServletContextConfig {
	private static final String PROPERTY_VIEW_PREFIX_KEY = "view.prefix";
	private static final String PROPERTY_VIEW_SUFFIX_KEY = "view.suffix";
	private final Environment environment;

	@Autowired
	public ServletContextConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setLocation(new ClassPathResource("config.properties"));
		return configurer;
	}

	@Bean
	public InternalResourceViewResolver viewResolver() {
		String prefix = environment.getProperty(PROPERTY_VIEW_PREFIX_KEY);
		String suffix = environment.getProperty(PROPERTY_VIEW_SUFFIX_KEY);
		return new InternalResourceViewResolver(prefix, suffix);
	}
}
