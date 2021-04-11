package repository.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import repository.Repository;

public class SimpleRepository implements Repository {
	private final JdbcTemplate jdbcTemplate;

	public SimpleRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public <T> List<T> selectList(String query, Map<String, Object> param, RowMapper<T> rowMapper) {
		return jdbcTemplate.query(query, rowMapper, param.values().toArray());
	}

	@Override
	public <T> T selectOne(String query, Map<String, Object> param, RowMapper<T> rowMapper) {
		return jdbcTemplate.queryForObject(query, rowMapper, param.values().toArray());
	}

	@Override
	public <T> T selectOne(String query, Map<String, Object> param, Class type) {
		return (T)jdbcTemplate.queryForObject(query, type, param.values().toArray());
	}
}
