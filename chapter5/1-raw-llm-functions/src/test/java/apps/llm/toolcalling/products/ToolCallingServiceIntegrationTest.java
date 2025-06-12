package apps.llm.toolcalling.products;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ToolCallingServiceIntegrationTest extends TestContainerIntegrationTest {

    Logger log = LoggerFactory.getLogger(ToolCallingServiceIntegrationTest.class);


    @Autowired
    ToolCallingService toolCallingService;

    @Test
    void WhenCalledWithFunctionDefinitionItShouldReturnFunction() throws Exception {

        String response = toolCallingService.chatWithInventory("Do you have Airpods Pro ");

        log.info("Response: {}", response);

        assertTrue(response.contains("AirPods Pro"), "Response should contain 'AirPods Pro'");




    }
}