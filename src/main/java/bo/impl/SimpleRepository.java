package bo.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import bo.Repository;

public class SimpleRepository implements Repository {
	private final JdbcTemplate jdbcTemplate;

	public SimpleRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public <T> List<T> select(String query, Map<String, Object> param, RowMapper<T> rowMapper) {
		return jdbcTemplate.query(query, rowMapper, param.values().toArray());
	}
}
