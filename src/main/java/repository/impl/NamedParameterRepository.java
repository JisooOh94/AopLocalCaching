package repository.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import repository.Repository;

public class NamedParameterRepository implements Repository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public NamedParameterRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}

	@Override
	public <T> List<T> selectList(String query, Map<String, Object> param, RowMapper<T> rowMapper) {
		return jdbcTemplate.query(query, param, rowMapper);
	}

	@Override
	public <T> T selectOne(String query, Map<String, Object> param, RowMapper<T> rowMapper) {
		return jdbcTemplate.queryForObject(query, param, rowMapper);
	}

	@Override
	public <T> T selectOne(String query, Map<String, Object> param, Class type) {
		return (T)jdbcTemplate.queryForObject(query, param, type);
	}
}
