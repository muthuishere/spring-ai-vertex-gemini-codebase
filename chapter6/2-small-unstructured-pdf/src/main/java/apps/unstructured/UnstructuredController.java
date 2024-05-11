package apps.unstructured;

import apps.unstructured.products.ProductAiService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.core.io.ClassPathResource;

import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/small-unstructured-text/")
@RequiredArgsConstructor
@Slf4j
public class UnstructuredController {

    private final ProductAiService productAiService;
    private final VertexAiGeminiChatClient vertexAiGeminiChatClient;


    @PostMapping("/chat-with-product")
    public ChatBotResponse chatWithProduct(@RequestBody ChatBotRequest chatBotRequest) {


        String question = chatBotRequest.question();

        String assistantContext= "You are an assistant, who can provide assistance with  information based on the data provided as attachments . You should answer only based on the data provided , You dont know any other stuff. \n";
        SystemMessage systemMessage = new SystemMessage(assistantContext);



        String productData = productAiService.readFromClasspath("vaccum-cleaner-products.txt");

        byte[] textData = productData.getBytes(Charset.defaultCharset());
        Media textMedia = new Media(MimeTypeUtils.TEXT_PLAIN, textData);


        var userMessage = new UserMessage(question,
                List.of(textMedia));

        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(userMessage);


        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        return new ChatBotResponse(question, answer);

    }


    @PostMapping("/chat-with-pdf")
    @SneakyThrows
    public ChatBotResponse chatWithPdf(@RequestBody ChatBotRequest chatBotRequest) {


        String question = chatBotRequest.question();

        String assistantContext= "You are an assistant, who can provide assistance with  information based on the data provided as attachment. You should answer only based on the data provided , You dont know any other stuff";
        SystemMessage systemMessage = new SystemMessage(assistantContext);




        byte[] productManualData = new ClassPathResource("ar_laptop_user_manual.pdf").getContentAsByteArray();
        Media pdfMedia = new Media(MimeTypeUtils.parseMimeType("application/pdf"), productManualData);


        var userMessage = new UserMessage(question,
                List.of(pdfMedia));

        var messages = new ArrayList<Message>();
        messages.add(systemMessage);
        messages.add(userMessage);


        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();


        return new ChatBotResponse(question, answer);

    }



}
