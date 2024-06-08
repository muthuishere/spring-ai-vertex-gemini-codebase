package apps.springai.products;

import apps.springai.ChatBotRequest;
import apps.springai.ChatBotResponse;
import apps.springai.products.sql.QueryRequest;
import apps.springai.products.sql.QueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductAiService productAiService;
    private final VertexAiGeminiChatClient vertexAiGeminiChatClient;
    private final   FunctionCallbackWrapper<QueryRequest, QueryResponse> llmSqlQueryExecutorFunction;


    @PostMapping("/chat-with-inventory")
    public ChatBotResponse chatWithInventory(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();
        ;

        String context = productAiService.readFromClasspath("generate-sql-function-prompt.txt");


        var messages = new ArrayList<Message>();
        messages.add(new SystemMessage(context));
        messages.add(new UserMessage(question));


        ChatOptions options = VertexAiGeminiChatOptions.builder()
                .withFunctionCallbacks(List.of(llmSqlQueryExecutorFunction))
                .build();

        Prompt prompt = new Prompt(messages,options);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();




        return new ChatBotResponse(question, answer);

    }



    public Optional<Product> getProductById(@PathVariable("id") String productId) {
        return productRepository.findById(productId);

    }


}
