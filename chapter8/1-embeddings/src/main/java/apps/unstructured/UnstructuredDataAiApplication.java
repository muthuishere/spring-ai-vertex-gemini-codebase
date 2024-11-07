package apps.unstructured;

import apps.unstructured.products.sql.LLMSqlQueryExecutor;
import apps.unstructured.products.sql.QueryRequest;
import apps.unstructured.products.sql.QueryResponse;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.model.function.FunctionCallbackContext;

@SpringBootApplication
public class UnstructuredDataAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnstructuredDataAiApplication.class, args);
    }


    @Bean
    public FunctionCallbackWrapper<QueryRequest, QueryResponse> llmSqlQueryExecutorFunction(LLMSqlQueryExecutor llmSqlQueryExecutor) {
        return FunctionCallbackWrapper.builder(llmSqlQueryExecutor)
                .withName("execute_sql_queries")
                .withSchemaType(FunctionCallbackContext.SchemaType.OPEN_API_SCHEMA)
                .withDescription("Execute SQL queries")
                .build();
    }
}
