package apps.unstructured.modelgarden;


import apps.unstructured.ChatBotRequest;
import apps.unstructured.ChatBotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ModelGardenController {

    private final OpenAiChatModel modelGardenOpenAiModel;
;


    @PostMapping("/api/llama-chat")
    public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

        String question = chatBotRequest.question();

        Prompt prompt = new Prompt(question);

        ChatResponse chatResponse = modelGardenOpenAiModel.call(prompt);
        log.info("Chat Response -> {}", chatResponse);

        String answer = chatResponse.getResult().getOutput().getText();
        return new ChatBotResponse(chatBotRequest.question(),  answer);
    }


}
