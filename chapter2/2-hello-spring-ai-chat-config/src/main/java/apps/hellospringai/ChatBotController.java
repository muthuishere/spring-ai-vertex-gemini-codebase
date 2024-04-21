package apps.hellospringai;


import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ChatBotController {


    private final VertexAiGeminiChatClient vertexAiGeminiChatClient;

    public ChatBotController(VertexAiGeminiChatClient vertexAiGeminiChatClient) {
        this.vertexAiGeminiChatClient = vertexAiGeminiChatClient;
    }


//    @PostMapping("/api/chat")
//    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
//
//        String answer = vertexAiGeminiChatClient.call(chatBotRequest.question());
//        return new ChatBotResponse(chatBotRequest.question(), answer);
//    }


    @PostMapping("/api/chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();
        ChatOptions options = VertexAiGeminiChatOptions.builder()
                .withMaxOutputTokens(100)
                .withTemperature(0.5f)
                .build();
        Prompt prompt = new Prompt(question, options);

        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        String answer = chatResponse.getResult().getOutput().getContent();
        return new ChatBotResponse(chatBotRequest.question(), answer);
    }




}
