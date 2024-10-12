package apps.unstructured;
import io.swagger.v3.oas.annotations.media.Schema;

public record SimilaritySearchRequest(String question, Integer limit, Double maximumDistance) {

    public SimilaritySearchRequest {
        if (limit == null) {
            limit = 4; // Default value for limit is set to 4
        }
        if (maximumDistance == null) {
            maximumDistance = 1.0; // Default value for threshold is set to 1.0
        }
    }

    @Schema(hidden = true)
    public  Double getThreshold() {
        return 1 - maximumDistance;
    }
}
