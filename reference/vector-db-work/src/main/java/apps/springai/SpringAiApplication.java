package apps.springai;

import apps.springai.products.sql.LLMSqlQueryExecutor;
import apps.springai.products.sql.QueryRequest;
import apps.springai.products.sql.QueryResponse;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }


    @Bean
    public FunctionCallbackWrapper<QueryRequest, QueryResponse> llmSqlQueryExecutorFunction(LLMSqlQueryExecutor llmSqlQueryExecutor) {
        return FunctionCallbackWrapper.builder(llmSqlQueryExecutor)
                .withName("execute_sql_queries")
                .withSchemaType(FunctionCallbackWrapper.Builder.SchemaType.OPEN_API_SCHEMA)
                .withDescription("Execute SQL queries")
                .build();
    }


}
