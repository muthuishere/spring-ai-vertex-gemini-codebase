package apps.unstructured;

import org.springframework.ai.document.Document;

import java.util.HashMap;
import java.util.Map;

public record DocumentRequest(String content, String department) {

    public Document getDocument() {
        Map<String, Object> metaData = new HashMap<>() ;
        metaData.put("department",department);
        return new Document(content,metaData);

    }

}