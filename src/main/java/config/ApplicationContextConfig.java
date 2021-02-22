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
	private static final String PROPERTIES_PATH = "config.properties";
	private static final String DDL_PATH = "classpath:sql\\schema.sql";
	private static final String DML_PATH = "classpath:sql\\data.sql";

	@Bean
	public PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
		properties.setLocation(new ClassPathResource(PROPERTIES_PATH));
		return properties;
	}

	@Bean
	public EmbeddedDatabase embeddedDatabase() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase embeddedDatabase = builder.setType(HSQL).addScripts(DDL_PATH, DML_PATH).build();
		return embeddedDatabase;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(embeddedDatabase());
		return jdbcTemplate;
	}
}
