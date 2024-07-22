package apps.springai.products.sql;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

@JsonClassDescription("Execute SQL queries request")
public record QueryRequest(@JsonProperty(required = true, value = "sqls")
                           @JsonPropertyDescription("The query to be executed as list of sqls ")  List<String> sqls) {
}
