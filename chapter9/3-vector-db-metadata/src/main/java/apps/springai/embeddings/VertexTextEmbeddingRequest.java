package apps.springai.embeddings;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class VertexTextEmbeddingRequest {

    private List<Instance> instances;
    public static VertexTextEmbeddingRequest createFrom(String text) {
        Instance instance =  Instance.builder().content(text).build();
        List<Instance> instances = new ArrayList<>();
        instances.add(instance);
        VertexTextEmbeddingRequest request = VertexTextEmbeddingRequest.builder().instances(instances).build();
        return request;
    }


    @Data
    @Builder
    public static class Instance {
        private String content;

    }

}

