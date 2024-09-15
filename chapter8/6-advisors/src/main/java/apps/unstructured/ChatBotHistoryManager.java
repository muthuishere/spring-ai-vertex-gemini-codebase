package apps.unstructured;


import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatBotHistoryManager {

    ConcurrentHashMap<String, List<Message>> chatBotHistory = new ConcurrentHashMap<>();


    public void addChatHistory(String sessionId, String question, String answer) {

        var existingHistory = getChatHistory(sessionId);
        var messages = new ArrayList<>(existingHistory);
        messages.add(new UserMessage(question));
        messages.add(new AssistantMessage(answer));
        chatBotHistory.put(sessionId, messages);
    }

    public List<Message> getChatHistory(String sessionId) {
        return chatBotHistory.getOrDefault(sessionId, new ArrayList<>());
    }

    public boolean isNewSession(String sessionId) {
        return !chatBotHistory.containsKey(sessionId);
    }

    public void addSystemMessage(String sessionId, String message) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(message));
        chatBotHistory.put(sessionId, messages);
    }

    public void deleteChatHistory(String sessionId) {
        chatBotHistory.remove(sessionId);
    }

}
