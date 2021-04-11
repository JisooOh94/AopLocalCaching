package repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public interface Repository {
	<T> List<T> selectList(String query, Map<String, Object> param, RowMapper<T> rowMapper);

	<T> T selectOne(String query, Map<String, Object> param, RowMapper<T> rowMapper);

	<T> T selectOne(String query, Map<String, Object> param, Class type);
}
