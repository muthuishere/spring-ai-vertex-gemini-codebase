package apps.hellospringai;


import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ChatBotController {


    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;

    public ChatBotController(VertexAiGeminiChatModel vertexAiGeminiChatModel) {
        this.vertexAiGeminiChatModel = vertexAiGeminiChatModel;
    }


//    @PostMapping("/api/chat")
//    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
//
//        String answer = vertexAiGeminiChatModel.call(chatBotRequest.question());
//        return new ChatBotResponse(chatBotRequest.question(), answer);
//    }


    @PostMapping("/api/chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
 
        String question = chatBotRequest.question();
        ChatOptions options = VertexAiGeminiChatOptions.builder()
                .maxOutputTokens(100)
                .temperature(0.5)
                .build();
        Prompt prompt = new Prompt(question, options);

        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        String answer = chatResponse.getResult().getOutput().getText();
        return new ChatBotResponse(chatBotRequest.question(), answer);
    }




}
