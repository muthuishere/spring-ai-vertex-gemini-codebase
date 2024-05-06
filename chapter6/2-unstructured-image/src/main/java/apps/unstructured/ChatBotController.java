package apps.unstructured;


import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController

public class ChatBotController {


    private final VertexAiGeminiChatClient vertexAiGeminiChatClient;
    private final ChatBotHistoryManager chatBotHistoryManager;

    public ChatBotController(VertexAiGeminiChatClient vertexAiGeminiChatClient, ChatBotHistoryManager chatBotHistoryManager) {
        this.vertexAiGeminiChatClient = vertexAiGeminiChatClient;
        this.chatBotHistoryManager = chatBotHistoryManager;
    }


    @PostMapping("/api/chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

        String sessionId = chatBotRequest.sessionId();
        String question = chatBotRequest.question();

        if (chatBotHistoryManager.isNewSession(sessionId))
            chatBotHistoryManager.addSystemMessage(sessionId, "You are my personal assistant");

        //Combine chat history with the new question
        var chatHistory = chatBotHistoryManager.getChatHistory(sessionId);
        var messages = new ArrayList<>(chatHistory);
        messages.add(new UserMessage(question));

        // create a prompt
        Prompt prompt = new Prompt(messages);


        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        // add the chat history to our local cache
        chatBotHistoryManager.addChatHistory(sessionId, question, answer);

        // return the answer
        return new ChatBotResponse(question, answer);


    }

    @PostMapping("/api/fact-check")
    public ChatBotResponse factCheck(@RequestBody ChatBotRequest chatBotRequest) {
        String sessionId = chatBotRequest.sessionId();
        String question = chatBotRequest.question();


        if (chatBotHistoryManager.isNewSession(sessionId))
            chatBotHistoryManager.addSystemMessage(sessionId, "You are my personal assistant, you should only answer true or false or not sure going forward.");

        //Combine chat history with the new question
        var chatHistory = chatBotHistoryManager.getChatHistory(sessionId);
        var messages = new ArrayList<>(chatHistory);


        messages.add(new UserMessage(question));

        // create a prompt
        Prompt prompt = new Prompt(messages);


        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        // add the chat history to our local cache
        chatBotHistoryManager.addChatHistory(sessionId, question, answer);

        return new ChatBotResponse(question, answer);

    }


    @DeleteMapping("/api/chat/{sessionId}")
    public ResponseEntity<Void> deleteChatHistory(@PathVariable String sessionId) {
        chatBotHistoryManager.deleteChatHistory(sessionId);
        return ResponseEntity.noContent().build();
    }


}
