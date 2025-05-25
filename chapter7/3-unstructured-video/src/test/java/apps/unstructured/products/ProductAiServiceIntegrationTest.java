package apps.unstructured.products;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ProductAiServiceIntegrationTest extends TestContainerIntegrationTest {


    @Autowired
    ProductAiService productAiService;

    Logger log = LoggerFactory.getLogger(ProductAiServiceIntegrationTest.class);

    @Test
    void testGenerateSqlQueryForMostExpensiveItem() {

        String chatBotResponse = productAiService.generateSqlQuery("What is the most expensive item in stock?");
        log.info(chatBotResponse);

        assertNotNull(chatBotResponse);
        assertTrue(chatBotResponse.contains("DESC LIMIT 1"));

    }

    @Test
    void testGenerateNaturalAnswerFromQueryAndResults() {

        String question = "What is the most expensive item in stock?";
        String query = productAiService.generateSqlQuery(question);
        log.info(query);
        String queryResults = productAiService.getJsonData(query);

        String results = productAiService.generateAnswer(question, query, queryResults);

        log.info(results);
        assertNotNull(results);
        assertTrue(results.contains("Canon"));
    }

    @Test
    void testGenerateNaturalAnswerFromValidData() {

        String question = "What is the most expensive item in stock?";
        String query = "SELECT name, price FROM inventory ORDER BY price DESC LIMIT 1";
        String queryResults = "[{\"name\":\"StyleCraft Smartwatch - Extended Warranty\",\"price\":1570.14}]";

        String results = productAiService.generateAnswer(question, query, queryResults);

        log.info(results);
        assertNotNull(results);
        assertTrue(results.contains("StyleCraft"));

    }
}