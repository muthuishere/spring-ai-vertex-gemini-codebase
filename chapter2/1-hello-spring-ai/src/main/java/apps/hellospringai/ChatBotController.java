package apps.hellospringai;


import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ChatBotController {


    private final VertexAiGeminiChatClient vertexAiGeminiChatClient;

    public ChatBotController(VertexAiGeminiChatClient vertexAiGeminiChatClient) {
        this.vertexAiGeminiChatClient = vertexAiGeminiChatClient;
    }


    @PostMapping("/api/chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

        String answer = vertexAiGeminiChatClient.call(chatBotRequest.question());
        return new ChatBotResponse(chatBotRequest.question(), answer);
    }




}
