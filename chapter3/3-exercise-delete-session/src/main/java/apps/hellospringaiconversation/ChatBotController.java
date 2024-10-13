package apps.hellospringaiconversation;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@Slf4j
public class ChatBotController {




    private  final  ChatClient inMemoryChatClient;
    private  final  ChatMemory chatMemory;


    public ChatBotController(VertexAiGeminiChatModel vertexAiGeminiChatModel ,ChatMemory chatMemory) {

        this.chatMemory = chatMemory;
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = new MessageChatMemoryAdvisor(chatMemory);
        inMemoryChatClient = ChatClient.builder(vertexAiGeminiChatModel)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
    }

    @PostMapping("/api/chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {
        String sessionId = chatBotRequest.sessionId();
        String question = chatBotRequest.question();


        var chatRequest = inMemoryChatClient
                .prompt()
                .system("You are my personal assistant")
                .user(question)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId));


        var chatResponse = chatRequest.call().chatResponse();

//        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        log.info("ChatBotController: askQuestion: answer: {}", answer);
        // return the answer
        return new ChatBotResponse(question, answer);


    }

    @DeleteMapping("/api/chat/{sessionId}")
    public ResponseEntity<Void> deleteChatHistory(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
        return ResponseEntity.noContent().build();
    }

}
