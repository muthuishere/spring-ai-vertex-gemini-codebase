package apps.springai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class VectorDbControllerIntegrationTest extends BaseVectorIntegrationTest {

    Logger log = LoggerFactory.getLogger(VectorDbControllerIntegrationTest.class);


    @Autowired

    VectorDbController vectorDbController;


    @Test
    public void testExistsShouldReturnFalse(){

        vectorDbController.deleteAllDocuments();

        assertFalse(vectorDbController.checkRowsExist());

        List<DocumentRequest> documents = List.of(
                new DocumentRequest("Our e-commerce platform's backend is built with Spring Boot, utilizing Spring Security for authentication and Spring Data JPA for database operations", "IT"),
                new DocumentRequest("The company's board of directors is led by Jane Smith, a seasoned e-commerce executive with 20 years of industry experience", "Executive Management"),
                new DocumentRequest("Our customer-facing web application is developed using React, providing a responsive and interactive shopping experience", "IT"),
                new DocumentRequest("Last quarter's revenue reached $24.7 million, marking a 15% year-over-year growth", "Executive Management"),
                new DocumentRequest("Employee attendance last month averaged 94%, with our flexible work-from-home policy contributing to high engagement", "HR"),
                new DocumentRequest("We leverage machine learning for personalized product recommendations, implemented using TensorFlow and Python", "IT"),
                new DocumentRequest("Our mobile app, available on both iOS and Android platforms, accounts for 40% of our total sales", "IT")
        );
        vectorDbController.saveDocumentsToVectorStore(documents);
        assertTrue(vectorDbController.checkRowsExist());

        List<Document> allRows = vectorDbController.findAllRows();

        log.info(allRows.toString());

    }


}