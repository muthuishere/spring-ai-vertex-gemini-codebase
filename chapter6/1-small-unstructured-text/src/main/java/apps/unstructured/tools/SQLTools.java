package apps.unstructured.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class SQLTools {

    private final JdbcTemplate jdbcTemplate;

    @Tool(description = "Execute the provided SQL Queries and get the response as JSON String")
    @SneakyThrows
    public String executeSqlQueries(List<String> sqls) {

        List<String> sqlQueryResults = executeSql(sqls);

        //Convert the list of results to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String sqlResultsInJsonFormat = objectMapper.writeValueAsString(sqlQueryResults);

        log.info("Returning response: " + sqlResultsInJsonFormat);
        return sqlResultsInJsonFormat;
    }

    private List<String> executeSql(List<String> sqlQueries) {
        List<String> sqlQueryResults = new ArrayList<>();
        for (String query : sqlQueries) {
            String jsonData = queryToJson(query);
            sqlQueryResults.add(jsonData);
        }
        return sqlQueryResults;
    }

    @SneakyThrows
    public String queryToJson(String sql) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(rows);

    }
}
