package apps.unstructured.products.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Component
public class LLMSqlQueryExecutor implements Function<QueryRequest, QueryResponse> {
    private final JdbcTemplate jdbcTemplate;



    @SneakyThrows
    public QueryResponse apply(QueryRequest queryRequest) {
        log.info("Processing request: " + queryRequest);

        List<String> sqls = queryRequest.sqls();
        List<String> sqlQueryResults = executeSqlQueries(sqls);

        //Convert the list of results to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String sqlResultsInJsonFormat = objectMapper.writeValueAsString(sqlQueryResults);

        log.info("Returning response: " + sqlResultsInJsonFormat);
        return new QueryResponse(sqlResultsInJsonFormat);
    }




    private List<String> executeSqlQueries(List<String> sqlQueries) {
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
