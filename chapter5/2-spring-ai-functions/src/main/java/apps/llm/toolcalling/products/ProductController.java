package apps.llm.toolcalling.products;

import apps.llm.toolcalling.ChatBotRequest;
import apps.llm.toolcalling.ChatBotResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;


    private ChatClient chatClient;

    private final ProductTools productTools;





    @PostMapping("/chat-with-inventory")
    public ChatBotResponse chatWithInventory(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();
        ;



        var messages = new ArrayList<Message>();
        messages.add(new UserMessage(question));



        Prompt prompt = new Prompt(messages);


        chatClient = ChatClient.builder(vertexAiGeminiChatModel)
                .defaultTools(productTools)
                .build();

        // call the chat client
        String answer = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Response: {}", answer);

        return new ChatBotResponse(question, answer);

    }


}
