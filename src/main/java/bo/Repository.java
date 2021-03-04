package bo;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public interface Repository {
	<T> List<T> select(String query, Map<String, Object> param, RowMapper<T> rowMapper);
}
