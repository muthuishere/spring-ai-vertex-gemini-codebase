package apps.springai.products;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SqlGeneratorIntegrationTest extends TestContainerIntegrationTest {


    @Autowired
    ProductAiService productAiService;

    Logger log = LoggerFactory.getLogger(SqlGeneratorIntegrationTest.class);

    @Test
    void testGenerateSqlQueryForMostExpensiveItem() {

        String chatBotResponse = productAiService.generateSqlQuery("What is the most expensive and least expensive item in inventory");
        log.info(chatBotResponse);

        assertNotNull(chatBotResponse);
        assertTrue(chatBotResponse.contains("DESC LIMIT 1"));

    }
}