package apps.naturalsqlai.products;

import apps.naturalsqlai.ChatBotRequest;
import apps.naturalsqlai.ChatBotResponse;
import apps.naturalsqlai.products.tools.SQLTools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
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

    private final JdbcTemplate jdbcTemplate;

    private ChatClient chatClient;

    @PostConstruct
    void setup() {
        chatClient = ChatClient.builder(vertexAiGeminiChatModel)
                .defaultSystem("You are a smart helpful assistant that can answer user questions, generate SQL query/queries for that user question, execute those SQL query/queries and respond in human understandable language.")
                .defaultTools(new SQLTools(jdbcTemplate))
                .build();
    }


    public String readFromClasspath(String filename) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new RuntimeException("No file named " + filename + " found in classpath");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from classpath", e);
        }
    }

    @PostMapping("/chat-with-inventory")
    public ChatBotResponse chatWithInventory(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();
        ;

        String context = readFromClasspath("generate-sql-tool-calling-prompt.txt");
        String assistantMessage = readFromClasspath("assistant-prompt-sql-tool-calling.txt");


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


}
