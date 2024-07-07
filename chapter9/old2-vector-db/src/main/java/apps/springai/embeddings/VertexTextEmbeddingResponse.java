package apps.springai.embeddings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.Embedding;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j


public class VertexTextEmbeddingResponse {

    private List<Prediction> predictions;

    public List<Embedding> getEmbeddings(){
        List<Embedding> results = new ArrayList<>();
        for (int i = 0; i < predictions.size(); i++) {
            Prediction prediction = predictions.get(i);

            List<Double> values = prediction.getEmbeddings().getValues();
            log.info(prediction.getEmbeddings().getStatistics().toString());
            results.add(new Embedding(values, i));
        }

        return results;


    }



    @Data
    public static class Prediction {

        private Embeddings embeddings;
    }

    @Data
    public static class Embeddings {

        private List<Double> values;
        private Statistics statistics;
    }

    @Data
    public static class Statistics {

        private boolean truncated;

        private int token_count;
    }




}
