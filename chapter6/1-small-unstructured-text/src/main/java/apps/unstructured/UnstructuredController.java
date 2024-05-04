package apps.unstructured;

import apps.unstructured.products.ProductAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

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

        String about= "You are an assistant, who can provide assistance with product information mentioned below. You should answer only based on below data , You dont know any other stuff. \n";

        String vaccumCleanerProductInformation = productAiService.readFromClasspath("vaccum-cleaner-products.txt");

        String systemMessageContext = about + vaccumCleanerProductInformation;


        var messages = new ArrayList<Message>();
        messages.add(new SystemMessage(systemMessageContext));
        messages.add(new UserMessage(question));




        Prompt prompt = new Prompt(messages);
        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatClient.call(prompt);

        log.info("Response: {}", chatResponse);
        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();




        return new ChatBotResponse(question, answer);

    }



}
