package apps.unstructured;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

@SpringBootTest
class EmbeddingMediaProcessorIntegrationTest {
    @Value("${spring.ai.vectorstore.pgvector.table-name}")
    private  String vectorTableName;


    @Autowired
    private  JdbcTemplate jdbcTemplate;

    @Autowired
    StorageBucketService storageBucketService;

    public void deleteAllVectors(){

        // delete all rows from table
        String sql = "DELETE FROM "+vectorTableName;

        jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
            return ps.executeUpdate();
        });

    }
@Test
    public void clearAll(){
    storageBucketService.deleteAllItemsInBucket();
        deleteAllVectors();

}



}