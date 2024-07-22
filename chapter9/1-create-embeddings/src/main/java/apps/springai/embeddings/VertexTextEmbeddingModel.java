package apps.springai.embeddings;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class VertexTextEmbeddingModel extends AbstractEmbeddingModel {

    private final AuthService authService;
    @Value("${spring.ai.vertex.ai.gemini.projectId}")
    private String projectId;
    @Value("${spring.ai.vertex.ai.gemini.location}")
    private String location;
    @Value("${spring.ai.vertex.ai.embedding.model}")
    private String embeddingModel;

    public VertexTextEmbeddingModel(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @SneakyThrows
    public EmbeddingResponse call(EmbeddingRequest embeddingRequest) {
        List<Embedding> embeddings = new ArrayList<>();
        for (String instruction : embeddingRequest.getInstructions()) {
            VertexTextEmbeddingResponse response = getEmbeddingValueForInstruction(instruction);
            embeddings.addAll(response.getEmbeddings());
        }
        return new EmbeddingResponse(embeddings);
    }

    VertexTextEmbeddingResponse getEmbeddingValueForInstruction(String instruction) {
        String apiUrl = "https://" + location + "-aiplatform.googleapis.com/v1/projects/" + projectId + "/locations/" + location + "/publishers/google/models/" + embeddingModel + ":predict";
        log.info("API URL: " + apiUrl);
        RestClient restClient = RestClient.builder().build();
        String googleAccessToken = authService.getAccessToken();
        VertexTextEmbeddingRequest embeddingRequest = VertexTextEmbeddingRequest.createFrom(instruction);
        return restClient
                .post().uri(apiUrl)
                .header("Authorization", "Bearer " + googleAccessToken)
                .body(embeddingRequest)
                .retrieve()
                .body(VertexTextEmbeddingResponse.class);
    }

    @Override
    public List<Double> embed(Document document) {
        return embed(document.getContent());
    }
}
