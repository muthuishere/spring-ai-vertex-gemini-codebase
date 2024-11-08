package apps.unstructured.llama3;

import apps.unstructured.ChatBotRequest;
import apps.unstructured.ChatBotResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LocallamaChatController {

    private final LocallamaChatModel locallamaChatModel;

    @SneakyThrows
    public CompletableFuture<String> executeJavaClassAsync(String className) {
        Class<?> clazz = Class.forName(className);
        log.info("clazz: {}", clazz);
        return CompletableFuture.supplyAsync(() -> {
            Process processRef = null;
            try {
                URL rootUrl = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource("");
                if (rootUrl == null) {
                    rootUrl = LocallamaChatController.class.getClassLoader()
                        .getResource("");
                }

                if (rootUrl == null) {
                    throw new RuntimeException(
                        "Cannot determine classes root directory"
                    );
                }

                File rootDir = new File(rootUrl.getPath());
                System.out.println(
                    "Root Directory: " + rootDir.getAbsolutePath()
                );

                String currentClassPath = clazz
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

                String javaHome = System.getProperty("java.home");
                String javaPath = javaHome + "/bin/java";

                log.info("javaPath: {}", javaPath);

                List<String> command = List.of(
                    javaPath,
                    "--enable-preview",
                    "--add-modules",
                    "jdk.incubator.vector",
                    className,
                    "--model",
                    "/Users/muthuishere/muthu/gitworkspace/courses/spring-ai-workspace/spring-ai-vertex-gemini-codebase/models/Llama-3.2-1B-Instruct-Q4_0.gguf",
                    "--max-tokens",
                    "512",
                    "--echo",
                    "true",
                    "--stream",
                    "false",
                    "--prompt",
                    "\"Why is the sky blue?\""
                );

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(rootDir);

                System.out.println("\nCommand to run manually:");
                System.out.println("------------------------");
                System.out.println(String.join(" ", command));
                System.out.println("------------------------\n");

                // Create final reference to process
                final Process process = processBuilder.start();
                processRef = process; // Store reference for cleanup in catch block

                StringBuilder output = new StringBuilder();

                CompletableFuture<String> outputFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try (
                            BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream())
                            )
                        ) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                                output.append(line).append("\n");
                            }
                            return output.toString();
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CompletionException(e);
                        }
                    });

                if (!process.waitFor(5, TimeUnit.MINUTES)) {
                    process.destroyForcibly();
                    throw new TimeoutException(
                        "Process execution timed out after 5 minutes"
                    );
                }

                String processOutput = outputFuture.get(1, TimeUnit.MINUTES);

                int exitCode = process.exitValue();
                return "Exit Code: " + exitCode + "\nOutput: " + processOutput;
            } catch (Throwable e) {
                log.error("Error executing process", e);
                if (processRef != null) {
                    processRef.destroyForcibly();
                }
                throw new CompletionException(
                    "Process execution failed: " + e.getMessage(),
                    e
                );
            }
        }).orTimeout(6, TimeUnit.MINUTES);
    }

    private void executeJavaClassWithSSE(SseEmitter emitter, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            log.info("clazz: {}", clazz);

            URL rootUrl = Thread.currentThread()
                .getContextClassLoader()
                .getResource("");
            if (rootUrl == null) {
                rootUrl = LocallamaChatController.class.getClassLoader()
                    .getResource("");
            }

            if (rootUrl == null) {
                throw new RuntimeException(
                    "Cannot determine classes root directory"
                );
            }

            File rootDir = new File(rootUrl.getPath());
            String javaHome = System.getProperty("java.home");
            String javaPath = javaHome + "/bin/java";

            List<String> command = List.of(
                javaPath,
                "--enable-preview",
                "--add-modules",
                "jdk.incubator.vector",
                className,
                "--model",
                "/Users/muthuishere/muthu/gitworkspace/courses/spring-ai-workspace/spring-ai-vertex-gemini-codebase/models/Llama-3.2-1B-Instruct-Q4_0.gguf",
                "--max-tokens",
                "512",
                "--echo",
                "true",
                "--stream",
                "false",
                "--prompt",
                "\"Why is the sky blue?\""
            );

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(rootDir);

            Process process = processBuilder.start();

            // Stream standard output
            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                )
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    emitter.send(finalLine, MediaType.TEXT_PLAIN);
                }
            }

            // Wait for process to complete
            if (!process.waitFor(5, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                emitter.send(
                    "Process execution timed out after 5 minutes",
                    MediaType.TEXT_PLAIN
                );
            }

            int exitCode = process.exitValue();
            emitter.send("Exit Code: " + exitCode, MediaType.TEXT_PLAIN);
            emitter.complete();
        } catch (Exception e) {
            log.error("Error executing process", e);
            try {
                emitter.send("Error: " + e.getMessage(), MediaType.TEXT_PLAIN);
                emitter.complete();
            } catch (IOException ioException) {
                emitter.completeWithError(ioException);
            }
        }
    }

    @GetMapping(path = "/execute", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeProcess() {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                executeJavaClassWithSSE(
                    emitter,
                    "apps.unstructured.llama3.Llama3"
                );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    @PostMapping("/api/llama-chat")
    public ChatBotResponse askQuestion(
        @RequestBody ChatBotRequest chatBotRequest
    ) {
        String question = chatBotRequest.question();
        Prompt prompt = new Prompt(question);
        log.info("prompt: {}", prompt);
        ChatResponse chatResponse = locallamaChatModel.call(prompt);
        log.info("chatResponse: {}", chatResponse);
        String answer = chatResponse.getResult().getOutput().getContent();
        return new ChatBotResponse(chatBotRequest.question(), answer);
    }
}
