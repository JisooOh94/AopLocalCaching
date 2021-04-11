package config;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import bo.UserInfoBo;
import dao.UserInfoDao;
import interceptor.ThreadLocalCacheInterceptor;
import repository.impl.NamedParameterRepository;
import repository.impl.SimpleRepository;

@Configuration
@EnableAspectJAutoProxy
public class ApplicationContextConfig {
	private static final String PROPERTIES_PATH = "config.properties";
	private static final String DDL_PATH = "classpath:sql\\schema.sql";
	private static final String DML_PATH = "classpath:sql\\data.sql";

	//BO===================================================================================================
	@Bean
	public UserInfoBo userInfoBo() {
		return new UserInfoBo(userInfoDao());
	}

	//Dao===================================================================================================
	@Bean
	public UserInfoDao userInfoDao() {
		return new UserInfoDao(namedParameterRepository());
	}

	//Properties===================================================================================================
	@Bean
	public PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
		properties.setLocation(new ClassPathResource(PROPERTIES_PATH));
		return properties;
	}

	//DB===================================================================================================
	@Bean
	public EmbeddedDatabase embeddedDatabase() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase embeddedDatabase = builder.setType(HSQL).addScripts(DDL_PATH, DML_PATH).build();
		return embeddedDatabase;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(embeddedDatabase());
	}

	@Bean
	public SimpleRepository simpleRepository() {
		return new SimpleRepository(jdbcTemplate());
	}

	@Bean
	public NamedParameterRepository namedParameterRepository() {
		return new NamedParameterRepository(jdbcTemplate());
	}

	@Bean
	public ThreadLocalCacheInterceptor localCacheRepository() { return new ThreadLocalCacheInterceptor(); }
}
