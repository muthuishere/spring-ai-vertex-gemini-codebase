package apps.unstructured;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;

public record InternalSearchRequest(String question, String category,
                                    String extension) {



    @Schema(hidden = true)
    public SearchRequest getSearchRequest() {

        StringBuilder expressionBuilder = new StringBuilder();

        if (this.extension() != null) {
            expressionBuilder.append("extension == '").append(this.extension()).append("'");
        }

        if (this.category() != null) {
            if (expressionBuilder.length() > 0) {
                expressionBuilder.append(" && ");
            }
            expressionBuilder.append("category == '").append(this.category()).append("'");
        }

        String expressionString = expressionBuilder.toString();
        var parser = new FilterExpressionTextParser();
        var expression = expressionString.isEmpty() ? null : parser.parse(expressionString);

        return SearchRequest.query(question).withFilterExpression(expression);

    }

}