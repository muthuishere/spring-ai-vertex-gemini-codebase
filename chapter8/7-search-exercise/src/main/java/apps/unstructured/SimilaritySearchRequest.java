package apps.unstructured;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public record SimilaritySearchRequest(String question, Integer limit, Double maximumDistance, String department) {

    public SimilaritySearchRequest {
        if (limit == null) {
            limit = 4; // Default value for limit is set to 4
        }
        if (maximumDistance == null) {
            maximumDistance = 1.0; // Default value for threshold is set to 1.0
        }
    }

 @Schema(hidden = true)
    public Double getThreshold() {
        return 1 - maximumDistance;
    }


 @Schema(hidden = true)
    public SearchRequest getSearchRequest() {


        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(limit)
                .similarityThreshold(getThreshold())
                .build();


        if(department != null) {
            Filter.Expression expression =
                    new FilterExpressionBuilder()
                            .eq("department", department)
                            .build();
            searchRequest = SearchRequest.from(searchRequest).filterExpression(expression).build();
        }

        return searchRequest;

    }

}
