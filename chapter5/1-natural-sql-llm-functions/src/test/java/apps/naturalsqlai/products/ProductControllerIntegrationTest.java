package apps.naturalsqlai.products;

import apps.naturalsqlai.ChatBotRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ProductControllerIntegrationTest extends TestContainerIntegrationTest {

    Logger log = LoggerFactory.getLogger(ProductControllerIntegrationTest.class);

    @Autowired
    ProductController productController;

    @Test
    void chatWithInventory() {

        ChatBotRequest chatBotRequest = new ChatBotRequest("What is the most expensive and least expensive item in inventory",null);

        var chatBotResponse = productController.chatWithInventory(chatBotRequest);
        log.info(chatBotResponse.toString());

        assertNotNull(chatBotResponse);
        assertTrue(chatBotResponse.answer().contains("The most expensive item"));

    }
}