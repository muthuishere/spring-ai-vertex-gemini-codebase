package apps.springai;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;






@SpringBootTest
class InternalChatControllerIntegrationTest extends BaseVectorIntegrationTest {

    Logger log = LoggerFactory.getLogger(InternalChatControllerIntegrationTest.class);

    @Autowired
    InternalChatController internalChatController;




    @Autowired

    VectorDbController vectorDbController;


    @ParameterizedTest
    @CsvSource({
            "How has your work policy affected team morale?,The most expensive item",
            "How is your platform built on?,React"
    })

    void testAnswerQuestion(String question , String answerContains) {

        //

        log.info("vectorDbController.findAllRows()" + vectorDbController.findAllRows().size());

        var similaritySearchRequest = new SimilaritySearchRequest(question,5,null,null);

        var chatBotResponse = internalChatController.answerQuestion(similaritySearchRequest);
        log.info(chatBotResponse.toString());

        assertNotNull(chatBotResponse);
        assertTrue(chatBotResponse.answer().contains(answerContains));

    }




}