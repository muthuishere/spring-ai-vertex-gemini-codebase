package apps.springai.products.sql;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;


@JsonClassDescription("Executed SQL queries response")
public record QueryResponse(@JsonProperty(required = true,

        value = "json") @JsonPropertyDescription("The response for all the executed queries in json string") String json) {
}
