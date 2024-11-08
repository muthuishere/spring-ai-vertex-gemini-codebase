package apps.standalonellm;

import apps.standalonellm.sse.SseHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StandalonellmApplication {

    @SneakyThrows
    public static void main(String[] args) {
        String modelPath =
            "/Users/muthuishere/muthu/gitworkspace/courses/spring-ai-workspace/spring-ai-vertex-gemini-codebase/models/Llama-3.2-1B-Instruct-Q4_0.gguf";
        int maxTokens = 5000;

        log.info("Initializing LLM model...");
        LocallamaChatModel model = new LocallamaChatModel(modelPath, maxTokens);

        log.info("Starting HTTP server...");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/chat", new SseHandler(model));
        server.setExecutor(null);
        server.start();

        log.info("Server started on port 8080");
    }
}
