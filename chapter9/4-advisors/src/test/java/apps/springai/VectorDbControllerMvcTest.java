package apps.springai;
import org.springframework.ai.document.Document;
import org.springframework.http.*;

import apps.springai.products.TestContainerIntegrationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@ExtendWith(SpringExtension.class)
public class VectorDbControllerMvcTest extends TestContainerIntegrationTest {

    Logger log = LoggerFactory.getLogger(VectorDbControllerMvcTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    private String baseUrl;


    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        // Add multiple documents
//        String jsonDocuments = "[{\"content\": \"Our e-commerce platform's backend is built with Spring Boot, utilizing Spring Security for authentication and Spring Data JPA for database operations\",\"department\": \"IT\"},{\"content\": \"The company's board of directors is led by Jane Smith, a seasoned e-commerce executive with 20 years of industry experience\",\"department\": \"Executive Management\"},{...}]";
        List<DocumentRequest> documents = List.of(
                new DocumentRequest("Our e-commerce platform's backend is built with Spring Boot, utilizing Spring Security for authentication and Spring Data JPA for database operations", "IT"),
                new DocumentRequest("The company's board of directors is led by Jane Smith, a seasoned e-commerce executive with 20 years of industry experience", "Executive Management"),
                new DocumentRequest("Our customer-facing web application is developed using React, providing a responsive and interactive shopping experience", "IT"),
                new DocumentRequest("Last quarter's revenue reached $24.7 million, marking a 15% year-over-year growth", "Executive Management"),
                new DocumentRequest("Employee attendance last month averaged 94%, with our flexible work-from-home policy contributing to high engagement", "HR"),
                new DocumentRequest("We leverage machine learning for personalized product recommendations, implemented using TensorFlow and Python", "IT"),
                new DocumentRequest("Our mobile app, available on both iOS and Android platforms, accounts for 40% of our total sales", "IT")
        );


//        restTemplate.postForEntity(baseUrl + "/api/vectordb/documents", getAsJson(documents), String.class);
        String url = baseUrl + "/api/vectordb/documents";
        String json = getAsJson(documents);
        ResponseEntity<String> response = getAsHttpResponse(url, json);
    }

    @AfterEach
    void tearDown() {
        // Delete all documents
        restTemplate.delete(baseUrl + "/api/vectordb/documents");
    }




    public  String getAsJson(Object searchRequest) {
//        SimilaritySearchRequest searchRequest = new SimilaritySearchRequest(question, limit, maximumDistance, department);

        ObjectMapper mapper = new ObjectMapper();

        String json = null;
        try {
            json = mapper.writeValueAsString(searchRequest);
            System.out.println(json);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
//    public  String getAsJson(List<Object> searchRequest) {
////        SimilaritySearchRequest searchRequest = new SimilaritySearchRequest(question, limit, maximumDistance, department);
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        String json = null;
//        try {
//            json = mapper.writeValueAsString(searchRequest);
//            System.out.println(json);
//            return json;
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//
//    }

    @Test
    public void testSearchDocumentsOnPlatformBuild() {

        SimilaritySearchRequest searchRequest = new SimilaritySearchRequest("How is your platform built on?", null, null, "IT");


        String url = baseUrl + "/api/vectordb/documents/search";
        String json = getAsJson(searchRequest);
        ResponseEntity<String> response = getAsHttpResponse(url, json);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Spring Boot")); // Check if response contains expected keywords
    }

    private ResponseEntity<String> getAsHttpResponse(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> request = new HttpEntity<>(json, headers);


        ResponseEntity<String> response = restTemplate.postForEntity(url,
                request, String.class);
        return response;
    }

    @Test
    public void testSearchDocumentsOnWorkPolicy() {
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/api/vectordb/documents/search",
                "{\"question\":\"How has your work policy affected team morale?\", \"limit\":2}", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("94%")); // Check if response contains expected output
    }


}