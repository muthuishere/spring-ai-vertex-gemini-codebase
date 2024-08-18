package apps.springai;

import org.springframework.ai.document.Document;

import java.util.Map;

public record DocumentRequest(String content, String department) {

    public Document getDocument() {
        Map<String, Object> metaData = Map.of("department", department);
        return new Document(content,metaData);

    }

}
