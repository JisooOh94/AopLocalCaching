package config;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

@Configuration
public class ApplicationContextConfig {
	@Bean
	public PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
		properties.setLocation(new ClassPathResource("config.properties"));
		return properties;
	}

	@Bean
	public EmbeddedDatabase embeddedDatabase() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase embeddedDatabase = builder.setType(HSQL).addScripts("classpath:sql\\schema.sql", "classpath:sql\\data.sql").build();
		return embeddedDatabase;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(embeddedDatabase());
		return jdbcTemplate;
	}
}
