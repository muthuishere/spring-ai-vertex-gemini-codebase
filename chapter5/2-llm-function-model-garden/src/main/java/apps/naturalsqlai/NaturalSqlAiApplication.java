package apps.naturalsqlai;

import apps.naturalsqlai.products.sql.LLMSqlQueryExecutor;
import apps.naturalsqlai.products.sql.QueryRequest;
import apps.naturalsqlai.products.sql.QueryResponse;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NaturalSqlAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NaturalSqlAiApplication.class, args);
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
