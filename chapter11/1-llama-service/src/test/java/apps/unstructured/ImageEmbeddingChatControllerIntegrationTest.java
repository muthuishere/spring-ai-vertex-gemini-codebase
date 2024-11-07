package apps.unstructured;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@SpringBootTest
@Slf4j
class ImageEmbeddingChatControllerIntegrationTest {



    @Autowired
    StorageBucketService storageBucketService;

    @Autowired
    ImageEmbeddingChatController internalAgentController;

    @Value("${spring.ai.vectorstore.pgvector.table-name}")
    private  String vectorTableName;


    @Autowired
    private JdbcTemplate jdbcTemplate;
    public void deleteAllVectors(){

        // delete all rows from table
        String sql = "DELETE FROM "+vectorTableName;

        jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
            return ps.executeUpdate();
        });

    }

    private void uploadFile(String filename, String category) throws IOException {
        ClassPathResource resource = new ClassPathResource("/testassets/" + filename);

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                filename,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                resource.getInputStream()
        );


        ResponseEntity<?> mapResponseEntity = internalAgentController.uploadFile(category, multipartFile);

        assert mapResponseEntity.getStatusCode().is2xxSuccessful();
    }

    @Test
    @SneakyThrows

    void  shouldUploadAnDocumentSuccessfullyAndEnquireitSuccesfullt(){

        deleteAllVectors();
        storageBucketService.deleteAllItemsInBucket();


        uploadFile("wikimedia_with_image.pdf", "offer");
        validateChatBotQuestion("What are the items available in wikimedia store", "shirt");

    }
    private void validateChatBotQuestion(String question, String containsString) {
        InternalSearchRequest req = new InternalSearchRequest(question,null,null);

        ChatBotResponse chatBotResponse = internalAgentController.answerQuestion(req);

        log.info("Response: {}", chatBotResponse);

        String answer = chatBotResponse.answer();

// How to setup the image references and document references for the project? , How did the LLM can happily say
        assert answer.toLowerCase().contains(containsString.toLowerCase());
    }

}