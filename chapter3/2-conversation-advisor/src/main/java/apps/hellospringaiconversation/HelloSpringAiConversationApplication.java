package apps.hellospringaiconversation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;

@SpringBootApplication
public class HelloSpringAiConversationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloSpringAiConversationApplication.class, args);
    }

    @Bean
    ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }



}
