package apps.llm.toolcalling.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductTools {

    private final ProductService productService;

    @Tool(description = "Finds a product based on its name or description or description"           )
    @SneakyThrows
    public String findProductByName(@ToolParam(description = "name or description of the product" ,required = true) String description) {

        List<Product> products = productService.findProductByName(description);

        //Convert the list of results to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String sqlResultsInJsonFormat = objectMapper.writeValueAsString(products);

        log.info("Returning response: " + sqlResultsInJsonFormat);
        return sqlResultsInJsonFormat;
    }
}
