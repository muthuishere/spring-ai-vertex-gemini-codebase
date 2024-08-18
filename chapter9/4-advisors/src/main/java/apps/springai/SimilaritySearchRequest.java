package apps.springai;


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
    public  Double getThreshold() {
        return 1 - maximumDistance;
    }


}

//public record SimilaritySearchRequest(String question,Integer limit) {
//
//    public SimilaritySearchRequest {
//        if (limit == null) {
//            limit = 4; // Default value for limit is set to 10
//        }
//    }
//}

//public record SimilaritySearchRequest(String question) {
//
//
//}
