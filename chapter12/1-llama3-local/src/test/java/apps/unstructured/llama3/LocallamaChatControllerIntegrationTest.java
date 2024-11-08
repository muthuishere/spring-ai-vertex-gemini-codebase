package apps.unstructured.llama3;

import apps.unstructured.ChatBotRequest;
import apps.unstructured.ChatBotResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class LocallamaChatControllerIntegrationTest {
    @Autowired
    LocallamaChatController locallamaChatController;

    @Autowired
    LocallamaChatModel locallamaChatModel;

    @Test
    void askQuestionWithLlama() {
        ChatBotRequest chatBotRequest = new ChatBotRequest("What is 1 + 1?");
//        ChatBotResponse chatBotResponse = locallamaChatController.askQuestion(chatBotRequest);
//
//        log.info("chatBotResponse: {}", chatBotResponse);
//        log.info(chatBotResponse.answer());


    }
}