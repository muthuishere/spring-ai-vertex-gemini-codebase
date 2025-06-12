# Accessing other models in GCP Model Garden

Up to this point, we have only utilized Vertex AI models with Spring AI. In this chapter, we will explore how to access and use other models available in the GCP Model Garden. Specifically, we will demonstrate how to integrate the LLaMA 3 service. Many of these models are compatible with OpenAI interfaces, ensuring that most implementations should work seamlessly.

## 11.1 Chapter Highlights:

* **GCP Setup**: Guided through enabling the Llama 3.1 API service in GCP Model Garden and configuring project settings.
* **Gradle Configuration**: Added necessary dependencies and configured `application.properties` for integrating Llama with Spring AI.
* **Authentication Handling**: Created a custom request interceptor to manage GCP access tokens for secure API requests.
* **Endpoint Creation**: Built a Spring Boot endpoint to handle user queries using the Llama API service for response generation.
* **Testing**: Demonstrated how to test the endpoint with a sample question and reviewed the format of expected responses.

## 11.1 Setting Up GCP

Log in to your GCP console and navigate to the Model Garden. Open the [Llama 3.1 API Service](https://console.cloud.google.com/vertex-ai/publishers/meta/model-garden/llama-3.1-405b-instruct-maas) and enable the API service. Once the API service is enabled, you're all set.

## 11.2 Setting Up Gradle and Configuration

Add the following dependency to your `build.gradle` file:

```groovy
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
```

This dependency will enable OpenAI configuration within your Spring AI project.

Configure the completion endpoint, base URL, and model in your `application.properties` file:

```properties
spring.ai.openai.chat.completions-path=/endpoints/openapi/chat/completions
spring.ai.openai.chat.base-url=https://us-central1-aiplatform.googleapis.com/v1beta1/projects/myspringai/locations/us-central1
spring.ai.openai.chat.options.model=meta/llama-3.2-90b-vision-instruct-maas
```

All necessary details can be found on the [Llama 3.1 API Service page](https://console.cloud.google.com/vertex-ai/publishers/meta/model-garden/llama-3.1-405b-instruct-maas?project=myspringai) where you enabled the service.

For now, if you want to focus only on the Llama integration, you can disable other AI services in your application.properties:

```properties
spring.ai.openai.embedding.enabled=false
```

Make sure you've set up your GCP credentials properly. The application assumes access to Google Cloud resources.

## 11.3 Setting Up Authentication Tokens for Llama Requests

Google APIs typically require application credentials that may change frequently. To manage this, create a request interceptor that will add an authorization header to each request made to the Model Garden Llama API.

### 11.3.1 Creating the Interceptor Class

Start by creating a class that extends `ClientHttpRequestInterceptor`:

```java
public class GcpModelGardenInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Add the authorization header here
        return execution.execute(request, body);
    }
}
```

Currently, this class does not modify the request. To make it functional, we need to add an authorization header so that GCP accepts the request.

### 11.3.2 Adding the Authorization Header

To obtain an access token, create a method that retrieves the token:

```java
@SneakyThrows
public String getAccessToken() {
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    credentials.refreshIfExpired();
    AccessToken token = credentials.getAccessToken();
    return token.getTokenValue(); // Returns the token value as a String
}
```

This method retrieves the token from the default application credentials, whether running locally or on GCP. The token refreshes only when it expires, ensuring efficient usage.

The updated intercept method will be like

```java
package apps.unstructured.modelgarden;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class GcpModelGardenInterceptor implements ClientHttpRequestInterceptor {

    @SneakyThrows
    public String getAccessToken() {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        credentials.refreshIfExpired();
        AccessToken token = credentials.getAccessToken();
        return token.getTokenValue(); // Returns the token value as a String
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setBearerAuth(getAccessToken());
        return execution.execute(request, body);
    }
}
```

### 11.3.3 Creating a Bean to Use Interceptor

Create a bean for the `RestClient.Builder` to use this interceptor:

```java
@Bean
public RestClient.Builder createRestClientBuilder() {
    return RestClient.builder()
            .requestInterceptor(new GcpModelGardenInterceptor());
}
```

You'll need to place this bean in a configuration class:

```java
@Configuration
@Slf4j
public class ModelGardenConfig {

    @Bean
    public RestClient.Builder createRestClientBuilder() {
        return RestClient.builder()
                .requestInterceptor(new GcpModelGardenInterceptor());
    }
}
```

This ensures that all requests made through the OpenAI library will pass through this `RestClient`, which applies the interceptor to set the access token.

### 11.3.4 Creating an Endpoint to Use Llama

Create a controller class with an autowired `OpenAiChatModel`:

```java
import apps.unstructured.ChatBotRequest;
import apps.unstructured.ChatBotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelGardenController {
    private final OpenAiChatModel modelGardenOpenAiModel;
}
```

Now, create a method for the `chat-llama` endpoint:

```java
@PostMapping("/api/llama-chat")
public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
    String question = chatBotRequest.question();
    Prompt prompt = new Prompt(question);
    ChatResponse chatResponse = modelGardenOpenAiModel.call(prompt);
    log.info("Chat Response -> {}", chatResponse);
    String answer = chatResponse.getResult().getOutput().getText();
    return new ChatBotResponse(chatBotRequest.question(), answer);
}
```

These components are straightforward, enabling seamless invocation of operations using the same model interface.

For the ChatBotRequest and ChatBotResponse, we can use Java records for simplicity:

```java
public record ChatBotRequest(String question) {
}

public record ChatBotResponse(String question, String answer) {
}
```

### 11.3.5 Testing the Endpoint

Now we will test the endpoint. You can create an HTTP request file (e.g., `requests/llama-chat-tests.http`) for easy testing:

```http
### Test Llama Chat
POST {{baseurl}}/api/llama-chat
Content-Type: application/json

{
  "question": "What is the god with single sword in hinduism?"
}
```

Make sure to define the `baseurl` variable in your environment file (e.g., `http-client.env.json`) or replace it with your actual server address.

The response will be something like this:

```json
{
  "question": "What is the god with single sword in hinduism?",
  "answer": "In Hindu mythology, the god often depicted with a single sword is Lord Murugan (also known as Kartikeya or Skanda). He is the god of war and the son of Lord Shiva.\n\nHowever, another god who is commonly depicted with a single sword is Lord Ayyappan (also known as Manikandan or Shasta). He is a deity revered in South India, particularly in the state of Kerala.\n\nBut if you're thinking of a god with a single sword who is more widely recognized across India, it's likely Lord Dattatreya. He is a deity who is often depicted with a single sword, along with a cow, and is considered to be an incarnation of the Trimurti (Brahma, Vishnu, and Shiva).\n\nHowever, the most widely recognized god with a single sword is probably Lord Parshuram (also known as Parasurama or Bhargava Rama). He is the sixth avatar (incarnation) of Lord Vishnu and is often depicted with a single sword, which is known as the Parashu.\n\nBut the god most commonly associated with a single sword is Lord Balarama, the brother of Lord Krishna, who is often depicted with a single sword known as the Halayudha."
}
```

You can also create an integration test for this endpoint:

```java
@SpringBootTest
@Slf4j
class ModelGardenControllerIntegrationTest {

    @Autowired
    ModelGardenController modelGardenController;

    @Test
    void askQuestionWithLlama() {
        ChatBotRequest chatBotRequest = new ChatBotRequest("What is 1 + 1?");
        ChatBotResponse chatBotResponse = modelGardenController.askQuestion(chatBotRequest);

        log.info("chatBotResponse: {}", chatBotResponse);
        assertNotNull(chatBotResponse);
        assertNotNull(chatBotResponse.question());
        assertNotNull(chatBotResponse.answer());
    }
    // Note: llama 3 api service does not support function calling
}
```

 >
 > The source code for this would be available on [Github](https://github.com/muthuishere/spring-ai-vertex-gemini-codebase/tree/main/chapter11/1-llama-service)

### 11.3.6 What We Learned:

* **GCP Model Garden Access**: How to enable and access models beyond standard Vertex AI services through GCP's Model Garden
* **Authentication Flow**: Implemented a robust authentication system using GCP credentials and request interceptors
* **OpenAI Compatibility**: Leveraged OpenAI-compatible interfaces to work with different model providers like Llama
* **Custom Integration**: Built a complete integration from configuration to endpoint creation for accessing Model Garden services
* **Token Management**: Implemented efficient token handling with automatic refresh capabilities for API authentication.
