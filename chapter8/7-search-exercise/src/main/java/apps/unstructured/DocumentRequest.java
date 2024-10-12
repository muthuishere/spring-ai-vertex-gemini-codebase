package apps.unstructured;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.ai.document.Document;

import java.util.HashMap;
import java.util.Map;

public record DocumentRequest(String content, String department) {

    /**
     * Creates a Document instance based on the content and department provided.
     * This method is crucial for the internal logic but does not need to be exposed
     * in the OpenAPI documentation.
     *
     * @Schema(hidden = true) - This annotation is strategically used to hide this method
     * in the Swagger-generated documentation, ensuring the API model is concise and
     * focused on the primary fields (content and department) used for API interactions.
     *
     * @return Document instance with the specified content and metadata.
     */
    @Schema(hidden = true)
    public Document getDocument() {
        Map<String, Object> metaData = new HashMap<>() ;
        metaData.put("department",department);
        return new Document(content,metaData);

    }

}