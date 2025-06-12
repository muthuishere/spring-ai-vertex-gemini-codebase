package apps.llm.toolcalling.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlTools {


    private final JdbcTemplate jdbcTemplate;
    ObjectMapper mapper = new ObjectMapper();


    @Tool(description = """
Executes a raw SQL SELECT query and returns the result as a JSON string.

This tool is intended for read-only, case-insensitive queries. For partial string matches, always use ILIKE 
or LOWER(column) LIKE LOWER('%keyword%') patterns instead of '='.

If you're unsure about table or column names, consider calling `getAllTableNames` or `getTableSchema` first.
""")
    @SneakyThrows
    public String executeSqlQuery(
            @ToolParam(description = "A valid SQL SELECT query to be executed on the database.", required = true)
            String query
    ) {
        log.info("Executing SQL query: {}", query);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
        String s = mapper.writeValueAsString(rows);

        log.info("Query executed successfully, returning results: {}", s);
        return s;
    }


    @Tool(description = "Returns a list of all table names in the current database usually all the products and inventory tables are available here.")
    @SneakyThrows
    public String getAllTableNames() {

        log.info("Fetching all table names from the database.");
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
        String s = mapper.writeValueAsString(tables);
        log.info("Fetched table names: {}", s);
        return s;
    }

    @Tool(description = "Returns the SQL schema definition (columns and types) of the specified table.")
    @SneakyThrows
    public String getTableSchema(
            @ToolParam(description = "Name of the table to get the schema for", required = true) String tableName
    ) {
        String sql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ?";
        log.info("Fetching schema for table: {}", tableName);
        List<Map<String, Object>> schema = jdbcTemplate.queryForList(sql, tableName);
        String s = mapper.writeValueAsString(schema);
        log.info("Fetched getTableSchema : {}", s);
        return s;
    }


}
