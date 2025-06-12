package apps.llm.toolcalling.products;

import apps.llm.toolcalling.ChatBotRequest;
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

        ChatBotRequest chatBotRequest = new ChatBotRequest("Do you have Airpods Pro ",null);

        var chatBotResponse = productController.chatWithInventory(chatBotRequest);
        log.info(chatBotResponse.toString());

        assertTrue(chatBotResponse.answer().contains("AirPods Pro"), "Response should contain 'AirPods Pro'");

    }


    @Test
    void chatWithInventoryForMultiple() {

        ChatBotRequest chatBotRequest = new ChatBotRequest("Do you have AirPods Pro or Samsung Galaxy?",null);

        var chatBotResponse = productController.chatWithInventory(chatBotRequest);
        log.info(chatBotResponse.toString());

        assertTrue(chatBotResponse.answer().contains("AirPods Pro"), "Response should contain 'AirPods Pro'");
        assertTrue(chatBotResponse.answer().contains("Galaxy"), "Response should contain 'Galaxy'");

    }


    @Test
    void chatWithInventorySql() {

        ChatBotRequest chatBotRequest = new ChatBotRequest("Do you have Airpods Pro ",null);

        var chatBotResponse = productController.chatWithSql(chatBotRequest);
        log.info(chatBotResponse.toString());

        assertTrue(chatBotResponse.answer().contains("AirPods Pro"), "Response should contain 'AirPods Pro'");

    }


    @Test
    void chatWithInventoryForMultipleSql() {


        ChatBotRequest chatBotRequest = new ChatBotRequest("Do you have AirPods Pro or Samsung Galaxy?",null);

        var chatBotResponse = productController.chatWithSql(chatBotRequest);
        log.info(chatBotResponse.toString());

        assertTrue(chatBotResponse.answer().contains("AirPods Pro"), "Response should contain 'AirPods Pro'");
        assertTrue(chatBotResponse.answer().contains("Galaxy"), "Response should contain 'Galaxy'");



    }
}