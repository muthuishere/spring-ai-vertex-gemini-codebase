package apps.unstructured;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DataEmbeddingController {
    private final EmbeddingModel embeddingModel;

    @PostMapping("/api/embeddings")
    public float[] embed(@RequestBody CreateEmbeddingRequest createEmbeddingRequest) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(createEmbeddingRequest.message()));
        float[] results = embeddingResponse.getResult().getOutput();

        return results;

    }
}
