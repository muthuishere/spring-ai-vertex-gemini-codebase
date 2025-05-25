package apps.unstructured.products;

import apps.unstructured.ChatBotRequest;
import apps.unstructured.ChatBotResponse;
import apps.unstructured.tools.SQLTools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductAiService productAiService;
    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;
    private final JdbcTemplate jdbcTemplate;

    private ChatClient chatClient;

    @PostConstruct
    void setup() {
        chatClient = ChatClient.builder(vertexAiGeminiChatModel)
                .defaultSystem("You are a smart helpful assistant that can answer user questions, generate SQL query/queries for that user question, execute those SQL query/queries and respond in human understandable language.")
                .defaultTools(new SQLTools(jdbcTemplate))
                .build();
    }


    @PostMapping("/chat-with-inventory")
    public ChatBotResponse chatWithInventory(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();

        String context = productAiService.readFromClasspath("generate-sql-tool-calling-prompt.txt");
        String assistantMessage = productAiService.readFromClasspath("generate-sql-tool-calling-prompt.txt");


        var messages = new ArrayList<Message>();
        messages.add(new UserMessage(context));
        messages.add(new AssistantMessage(assistantMessage));
        messages.add(new UserMessage(question));


        Prompt prompt = new Prompt(messages);

        // call the chat client
        String answer = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Response: {}", answer);

        return new ChatBotResponse(question, answer);
    }



    public Optional<Product> getProductById(@PathVariable("id") String productId) {
        return productRepository.findById(productId);

    }


}
