package apps.unstructured;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/unstructured-data/")
@RequiredArgsConstructor
@Slf4j
public class UnstructuredController {

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;


    @PostMapping("/chat-with-video")
    @SneakyThrows
    public ChatBotResponse chatWithProductVideo(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();

        String assistantContext= "You are an assistant, who can provide assistance with  information based on video . You should answer only based on video , You dont know any other stuff. ";
        SystemMessage systemMessage = new SystemMessage(assistantContext);


        ClassPathResource data = new ClassPathResource("/vaccum-ad.mp4");
        Media media = new Media(MimeTypeUtils.parseMimeType("video/mp4"), data);

        var userMessage = UserMessage.builder()
                .text(question)
                .media(media)
                .build();


        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(userMessage);

        Prompt prompt = new Prompt(messages);

        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        log.info("Response: {}", chatResponse);

        // get the answer
        String answer = chatResponse.getResult().getOutput().getText();

        return new ChatBotResponse(question, answer);

    }
}
