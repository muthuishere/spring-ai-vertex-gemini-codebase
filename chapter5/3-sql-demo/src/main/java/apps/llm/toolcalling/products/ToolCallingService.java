package apps.llm.toolcalling.products;


import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ToolCallingService {
    private final ProductService productService;


    String model="google/gemini-2.0-flash";

    @Value("${rawllm.endpoint}")
    String endpoint;
    


    

    private RestTemplate rest = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    // Template file paths
    private static final String FUNCTION_RESPONSE_TEMPLATE_PATH = "classpath:static/tool-calling-response-template.json";
    private static final String TOOL_CALLING_TEMPLATE_PATH = "classpath:static/tool-calling-template.json";


    private String loadTemplate(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    public String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            return token.getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException("Unable to fetch Google access token", e);
        }
    }



    public String processToolResponse(String question, String fnName, String args, String fnResponse, String toolCallId) throws IOException {
        // Load the template
        String template = loadTemplate(FUNCTION_RESPONSE_TEMPLATE_PATH);
        
        // Replace placeholders with actual values
        String payload = createToolResponseJson(
                model,
                question,
                args,
                toolCallId, // dynamic tool call ID
                fnResponse);

        String resp = callApi(payload);

        // Parse response manually
        JsonNode response = mapper.readTree(resp);
        JsonNode choice = response.path("choices").get(0);

        String text = choice.path("message").path("content").asText();
        return text;
    }

    private String callApi(String payload) {
        HttpHeaders hdr = new HttpHeaders();
        hdr.setBearerAuth(getAccessToken());
        hdr.setContentType(MediaType.APPLICATION_JSON);
        String resp = rest.postForObject(endpoint, new HttpEntity<>(payload, hdr), String.class);
        return resp;
    }

    public static ArrayNode extractToolCalls(JsonNode choice) {
        if (choice == null) return null;

        JsonNode toolCallsNode = choice.path("message").path("tool_calls");
        if (toolCallsNode != null && toolCallsNode.isArray()) {
            return (ArrayNode) toolCallsNode;
        }

        return null;
    }

    public String createToolResponseJson(
            String model,
            String question,
            String args,
            String toolCallId,
            String toolCallResponse
    ) {
        try {
           String rawJson = loadTemplate(FUNCTION_RESPONSE_TEMPLATE_PATH);
            // Set top-level model
            JsonNode root = mapper.readTree(rawJson);

            ((ObjectNode) root).put("model", model);

            // Set message[0] - user question
            ArrayNode messages = (ArrayNode) root.get("messages");
            ((ObjectNode) messages.get(0)).put("content", question);

            // Set message[1] - tool_calls
            ObjectNode toolCall = (ObjectNode) messages.get(1).get("tool_calls").get(0);
            toolCall.put("id", toolCallId);
            // ✅ insert args as literal string
            toolCall.with("function").put("arguments", args);

            // Set message[2] - tool response
            ObjectNode toolResponse = (ObjectNode) messages.get(2);
            toolResponse.put("tool_call_id", toolCallId);
            // ✅ insert response as literal string
            toolResponse.put("content", toolCallResponse);

            // Final JSON string
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build function call JSON", e);
        }
    }




    public String createQueryJson(
            String model,
            String question

    ) {
        try {

            String rawJson = loadTemplate(TOOL_CALLING_TEMPLATE_PATH);

            // Replace placeholders
            rawJson = rawJson
                    .replace("%model%", model)
                    .replace("%question%", question);


            // Validate and pretty-print JSON
            JsonNode finalJson = mapper.readTree(rawJson);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalJson);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build function call JSON", e);
        }
    }



    @SneakyThrows
    public String chatWithInventory(String question)  {

        
        // Replace placeholders with actual values
        String payload = createQueryJson( model, question);

        // POST it
        String resp = callApi(payload);

        // Parse response manually
        JsonNode response = mapper.readTree(resp);
        JsonNode choice = response.path("choices").get(0);


            ArrayNode calls = extractToolCalls(choice);


            if (calls.size() > 0) {


            JsonNode fn = calls.get(0).path("function");
            String fnName = fn.path("name").asText();


            // Manual dispatch
            if ("findProductByName".equals(fnName)) {
                String args = fn.path("arguments").asText();
                String toolCallId=calls.get(0).path("id").asText();


                //convert to json object
                JsonNode parameterInstance = mapper.readTree(args);
                String name = parameterInstance.path("name").asText();

                System.out.println("🔍 findProductByName → " + name);
                List<Product> productResponses = productService.findProductByName(name);

                String productResponsesAsString = mapper.writeValueAsString(productResponses);
                          return processToolResponse(question, fnName, args, productResponsesAsString,toolCallId);
            }
            else {
                throw new RuntimeException("Unknown function: " + fnName);
            }
        } else {
            String text = choice.path("message").path("content").asText();
            return text;
        }

    }

}
