package apps.naturalsqlai.products;

import apps.naturalsqlai.ChatBotRequest;
import apps.naturalsqlai.ChatBotResponse;
import apps.naturalsqlai.products.sql.QueryRequest;
import apps.naturalsqlai.products.sql.QueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;
    private final FunctionCallbackWrapper<QueryRequest, QueryResponse> llmSqlQueryExecutorFunction;


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

        String context = readFromClasspath("generate-sql-function-prompt.txt");


        var messages = new ArrayList<Message>();
        messages.add(new SystemMessage(context));
        messages.add(new UserMessage(question));


        ChatOptions options = VertexAiGeminiChatOptions.builder()
                .withFunctionCallbacks(List.of(llmSqlQueryExecutorFunction))
                .build();

        Prompt prompt = new Prompt(messages, options);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        return new ChatBotResponse(question, answer);

    }


}
