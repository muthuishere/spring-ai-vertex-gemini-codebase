package apps.standalonellm.sse;

import java.io.PrintWriter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SseClient {

    private final String id;
    private final PrintWriter writer;

    public SseClient(PrintWriter writer) {
        this.id = UUID.randomUUID().toString();
        this.writer = writer;
    }

    public void sendEvent(String data) {
        writer.write("data: " + data + "\n\n");
        writer.flush();
    }

    public String getId() {
        return id;
    }
}
