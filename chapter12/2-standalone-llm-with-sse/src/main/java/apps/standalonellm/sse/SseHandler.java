package apps.standalonellm.sse;

import apps.standalonellm.LocallamaChatModel;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SseHandler implements HttpHandler {

    private final LocallamaChatModel model;

    public SseHandler(LocallamaChatModel model) {
        this.model = model;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            handleSseConnection(exchange);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePostRequest(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleSseConnection(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);

        PrintWriter writer = new PrintWriter(exchange.getResponseBody());
        SseClient client = new SseClient(writer);

        // Keep connection alive
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String question = new String(exchange.getRequestBody().readAllBytes());

        CompletableFuture.runAsync(() -> {
            try {
                String answer = model.getAnswer(question);
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, answer.getBytes().length);
                exchange.getResponseBody().write(answer.getBytes());
            } catch (Exception e) {
                log.error("Error processing request", e);
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (IOException ioException) {
                    log.error("Error sending error response", ioException);
                }
            } finally {
                exchange.close();
            }
        });
    }
}
