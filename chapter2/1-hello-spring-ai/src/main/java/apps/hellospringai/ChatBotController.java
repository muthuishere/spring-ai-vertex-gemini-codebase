package apps.hellospringai;


import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ChatBotController {


    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;

    public ChatBotController(VertexAiGeminiChatModel vertexAiGeminiChatModel) {
        this.vertexAiGeminiChatModel = vertexAiGeminiChatModel;
    }


    @PostMapping("/api/chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

        String answer = vertexAiGeminiChatModel.call(chatBotRequest.question());
        return new ChatBotResponse(chatBotRequest.question(), answer);
    }




}
