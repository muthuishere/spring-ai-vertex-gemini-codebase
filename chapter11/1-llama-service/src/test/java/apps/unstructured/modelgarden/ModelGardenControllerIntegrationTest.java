package apps.unstructured.modelgarden;

import apps.unstructured.ChatBotRequest;
import apps.unstructured.ChatBotResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@Slf4j
class ModelGardenControllerIntegrationTest {

    @Autowired
    ModelGardenController modelGardenController;

    @Test
    void askQuestionWithLlama() {
        ChatBotRequest chatBotRequest = new ChatBotRequest("What is 1 + 1?");
        ChatBotResponse chatBotResponse = modelGardenController.askQuestion(chatBotRequest);

        log.info("chatBotResponse: {}", chatBotResponse);
        assertNotNull(chatBotResponse);
        assertNotNull(chatBotResponse.question());
        assertNotNull(chatBotResponse.answer());
    }
    // llama 3 api service does not support function
}
