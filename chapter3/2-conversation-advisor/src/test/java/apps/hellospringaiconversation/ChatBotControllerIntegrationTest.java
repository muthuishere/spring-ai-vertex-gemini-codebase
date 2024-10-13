package apps.hellospringaiconversation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ChatBotControllerIntegrationTest {

    @Autowired
    ChatBotController chatBotController;

    @Autowired
    ChatBotVerboseController chatBotVerboseController;

    @Test
    void testingOldManualChatWithHistoryShouldWorkFIne(){

        ChatBotRequest firstQuestion = new ChatBotRequest("My name is richard", "1234");
        ChatBotResponse chatBotResponse = chatBotVerboseController.askQuestionOld(firstQuestion);

        log.info("chatBotResponse: {}", chatBotResponse);
        var secondQuestion = new ChatBotRequest("What is my name", "1234");

        var chatBotResponse2 = chatBotVerboseController.askQuestionOld(secondQuestion);

        log.info("chatBotResponse2: {}", chatBotResponse2);
        assertThat(chatBotResponse2.answer(), org.hamcrest.Matchers.containsStringIgnoringCase("richard")  );


    }
    @Test
    void testingNewChatWithAdvisorShouldWorkFIne(){

        ChatBotRequest firstQuestion = new ChatBotRequest("My name is richard", "1234");
        ChatBotResponse chatBotResponse = chatBotController.askQuestion(firstQuestion);
        log.info("chatBotResponse: {}", chatBotResponse);
        var secondQuestion = new ChatBotRequest("What is my name", "1234");

        var chatBotResponse2 = chatBotController.askQuestion(secondQuestion);

        log.info("chatBotResponse2: {}", chatBotResponse2);
        assertThat(chatBotResponse2.answer(), org.hamcrest.Matchers.containsStringIgnoringCase("richard")  );


    }
}