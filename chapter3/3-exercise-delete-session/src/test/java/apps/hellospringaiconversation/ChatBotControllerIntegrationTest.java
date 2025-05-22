package apps.hellospringaiconversation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.not;

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
        assertThat(chatBotResponse2.answer(), containsStringIgnoringCase("richard")  );


    }
    @Test
    void testingNewChatWithAdvisorShouldWorkFIne(){

        ChatBotRequest firstQuestion = new ChatBotRequest("My name is richard", "1234");
        ChatBotResponse chatBotResponse = chatBotController.askQuestion(firstQuestion);
        log.info("chatBotResponse: {}", chatBotResponse);
        var secondQuestion = new ChatBotRequest("What is my name", "1234");

        var chatBotResponse2 = chatBotController.askQuestion(secondQuestion);

        log.info("chatBotResponse2: {}", chatBotResponse2);
        assertThat(chatBotResponse2.answer(), containsStringIgnoringCase("richard")  );


    }

    @Test
    void deletingWithSessionIdShouldDeleteConversation(){
        ChatBotRequest firstQuestion = new ChatBotRequest("My name is richard", "1234");
        ChatBotResponse chatBotResponse = chatBotController.askQuestion(firstQuestion);
        log.info("chatBotResponse: {}", chatBotResponse);
        var secondQuestion = new ChatBotRequest("What is my name", "1234");

        var chatBotResponse2 = chatBotController.askQuestion(secondQuestion);

        log.info("chatBotResponse2: {}", chatBotResponse2);
        assertThat(chatBotResponse2.answer(), containsStringIgnoringCase("richard")  );


        chatBotController.deleteChatHistory("1234");

        var chatBotResponse3 = chatBotController.askQuestion(secondQuestion);

        log.info("chatBotResponse3: {}", chatBotResponse3);

        assertThat(chatBotResponse3.answer(), not(containsStringIgnoringCase("richard")  ));




    }
}