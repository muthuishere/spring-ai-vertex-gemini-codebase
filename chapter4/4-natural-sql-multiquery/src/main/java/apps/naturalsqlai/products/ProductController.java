package apps.naturalsqlai.products;



import apps.naturalsqlai.ChatBotRequest;
import apps.naturalsqlai.ChatBotResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;

    private final ProductAiService productAiService;

    @GetMapping("/{id}")
    public Optional<Product> getProductById(@PathVariable("id") String productId) {
       return productRepository.findById(productId);

    }
    @SneakyThrows
    private static String getInJsonFormat(List<String> sqlQueryResults)  {
        ObjectMapper objectMapper = new ObjectMapper();
        String sqlResultsInJsonFormat = objectMapper.writeValueAsString(sqlQueryResults);
        return sqlResultsInJsonFormat;
    }

    @SneakyThrows
    private static List<String> convertJsonToArray(String jsonInput)  {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonInput, new TypeReference<List<String>>() {});

    }

    private List<String> executeSqlQueries(List<String> sqlQueries) {
        List<String> sqlQueryResults = new ArrayList<>();
        for (String query : sqlQueries) {
            String jsonData = productAiService.getJsonData(query);
            sqlQueryResults.add(jsonData);
        }
        return sqlQueryResults;
    }
    @PostMapping("/chat-with-inventory")
    public ChatBotResponse chatWithInventory(@RequestBody ChatBotRequest chatBotRequest) {
        try {
            String userQuestion = chatBotRequest.question();
            String sqlQueriesInJsonFormat = productAiService.generateSqlQuery(userQuestion);
            List<String> sqlQueries = convertJsonToArray(sqlQueriesInJsonFormat);
            List<String> sqlQueryResults = executeSqlQueries(sqlQueries);
            String sqlResultsInJsonFormat = getInJsonFormat(sqlQueryResults);

            log.info("SQL Queries: {}", sqlQueriesInJsonFormat);
            log.info("SQL results JSON: {}", sqlResultsInJsonFormat);

            String chatBotAnswer = productAiService.generateAnswer(userQuestion, sqlQueriesInJsonFormat, sqlResultsInJsonFormat);
            return new ChatBotResponse(userQuestion, chatBotAnswer);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing chat request", exception);
        }
    }



}
