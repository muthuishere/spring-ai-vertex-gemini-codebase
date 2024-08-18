package apps.springai;

import apps.springai.products.TestContainerIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BaseVectorIntegrationTest extends TestContainerIntegrationTest {



    Logger log = LoggerFactory.getLogger(BaseVectorIntegrationTest.class);


    @Autowired
    VectorDbController vectorDbController;


    @PostConstruct
    public void initData(){

        if(vectorDbController.checkRowsExist() == false){
            log.info("No data found in the database, adding sample data");
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
        }
    }
}