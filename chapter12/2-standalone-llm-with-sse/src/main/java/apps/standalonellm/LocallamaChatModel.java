package apps.standalonellm;

import static apps.standalonellm.Llama3.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocallamaChatModel {

    private final String modelPath;
    private final int maxTokens;
    private Llama localLlamaModel;
    private final Sampler sampler;

    @SneakyThrows
    public LocallamaChatModel(String modelPath, int maxTokens) {
        this.modelPath = modelPath;
        this.maxTokens = maxTokens;
        localLlamaModel = AOT.tryUsePreLoaded(
            Path.of(this.modelPath),
            maxTokens
        );
        if (localLlamaModel == null) {
            // No compatible preloaded model found, fallback to fully parse and load the specified file.
            localLlamaModel = ModelLoader.loadModel(
                Path.of(this.modelPath),
                maxTokens,
                true
            );
        }

        float temperature = 0.1f;
        float topp = 0.95f;
        long seed = System.nanoTime();
        sampler = selectSampler(
            localLlamaModel.configuration().vocabularySize,
            temperature,
            topp,
            seed
        );

        String answer = getAnswer("Hello , Whats your name?");

        log.info("Answer: {}", answer);
    }

    public String getAnswer(String question)
        throws InterruptedException, ExecutionException, TimeoutException {
        Llama.State state = localLlamaModel.createNewState();
        ChatFormat chatFormat = new ChatFormat(localLlamaModel.tokenizer());
        Set<Integer> stopTokens = chatFormat.getStopTokens();
        List<Integer> promptTokens = getInputTokens(question, chatFormat);

        log.info(
            "Starting token generation for prompt length: {}",
            promptTokens.size()
        );
        log.info("Prompt tokens: {}", promptTokens);
        log.info("Stop tokens: {}", stopTokens);

        System.gc(); // Request garbage collection before heavy operation
        // Add timeout to detect if it's hanging
        CompletableFuture<List<Integer>> future = CompletableFuture.supplyAsync(
            () ->
                Llama.generateTokens(
                    localLlamaModel,
                    state,
                    0,
                    promptTokens,
                    stopTokens,
                    maxTokens,
                    sampler,
                    false,
                    token -> {
                        // log.debug("Generated token: {}", token);
                    }
                )
        );

        List<Integer> responseTokens = future.get(30, TimeUnit.MINUTES); // 30 second timeout
        log.info("Token generation completed successfully");

        if (
            !responseTokens.isEmpty() &&
            stopTokens.contains(responseTokens.getLast())
        ) {
            responseTokens.removeLast();
        }

        String answer = localLlamaModel.tokenizer().decode(responseTokens);
        return answer;
    }

    private List<Integer> getInputTokens(
        String question,
        ChatFormat chatFormat
    ) {
        List<Integer> promptTokens = new ArrayList<>();
        promptTokens.add(chatFormat.beginOfText);
        ChatFormat.Message message = new ChatFormat.Message(
            ChatFormat.Role.USER,
            question
        );
        promptTokens.addAll(chatFormat.encodeMessage(message));

        ChatFormat.Message emptyEndMessage = new ChatFormat.Message(
            ChatFormat.Role.ASSISTANT,
            ""
        );
        promptTokens.addAll(chatFormat.encodeHeader(emptyEndMessage));
        return promptTokens;
    }
}
