package apps.springai.embeddings;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class VertexTextEmbeddingModelIntegrationTest {


    @Autowired
    VertexTextEmbeddingModel textEmbeddingClient;

    Logger log = LoggerFactory.getLogger(VertexTextEmbeddingModelIntegrationTest.class);

    @Test
    void testEmbeddingReturnsExpectedSize() {


        String question = "What is the average rating for all products in the brand apple?";
        float[] embeddings = textEmbeddingClient.embed(question);
        assertNotNull(embeddings);

        assertEquals(768, embeddings.length);

        log.info(embeddings.toString());
        log.info("size " + embeddings.length);

    }


}