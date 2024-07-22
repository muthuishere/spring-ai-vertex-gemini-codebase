package apps.unstructured;

import apps.unstructured.products.ProductAiService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/unstructured-data/")
@RequiredArgsConstructor
@Slf4j
public class UnstructuredController {

    private final ProductAiService productAiService;
    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;


    @PostMapping("/chat-with-product")
    public ChatBotResponse chatWithProduct(@RequestBody ChatBotRequest chatBotRequest) {


        String assistantContext = "You are an assistant, who can provide assistance with product information mentioned below. You should answer only based on below data , You dont know any other stuff. \n";

        String productData = productAiService.readFromClasspath("vacuum-cleaner-products.txt");

        String chatPromptContext = assistantContext + productData;

        SystemMessage systemMessage = new SystemMessage(chatPromptContext);

        String question = chatBotRequest.question();
        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(new UserMessage(question));


        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        return new ChatBotResponse(question, answer);

    }


    @PostMapping("/chat-with-image")
    @SneakyThrows
    public ChatBotResponse chatWithProductImage(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();

        String assistantContext= "You are an assistant, who can provide assistance with  information based on image . You should answer only based on image , You dont know any other stuff. \n";
        SystemMessage systemMessage = new SystemMessage(assistantContext);


        byte[] data = new ClassPathResource("/coupons.png").getContentAsByteArray();
        Media media = new Media(MimeTypeUtils.IMAGE_PNG, data);

        var userMessage = new UserMessage(question,
                List.of(media));


        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(userMessage);




        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();




        return new ChatBotResponse(question, answer);

    }


    @PostMapping("/chat-with-video")
    @SneakyThrows
    public ChatBotResponse chatWithProductVideo(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();

        String assistantContext= "You are an assistant, who can provide assistance with  information based on video . You should answer only based on video , You dont know any other stuff. ";
        SystemMessage systemMessage = new SystemMessage(assistantContext);


        byte[] data = new ClassPathResource("/vaccum-ad.mp4").getContentAsByteArray();
        Media media = new Media(MimeTypeUtils.parseMimeType("video/mp4"), data);

        var userMessage = new UserMessage(question,
                List.of(media));


        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(userMessage);




        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();




        return new ChatBotResponse(question, answer);

    }

    @PostMapping("/chat-with-audio")
    @SneakyThrows
    public ChatBotResponse chatWithAudio(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();

        String assistantContext= "You are an assistant, who can provide assistance with  information based on audio. You should answer the question only based on audio, You dont know any other stuff. Be precise of whats been asked and answer accordingly \n";
        SystemMessage systemMessage = new SystemMessage(assistantContext);


        byte[] data = new ClassPathResource("/sarah-customercare.mp3").getContentAsByteArray();
        Media media = new Media(MimeTypeUtils.parseMimeType("audio/mp3"), data);

        var userMessage = new UserMessage(question,
                List.of(media));


        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(userMessage);




        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();




        return new ChatBotResponse(question, answer);

    }



}
