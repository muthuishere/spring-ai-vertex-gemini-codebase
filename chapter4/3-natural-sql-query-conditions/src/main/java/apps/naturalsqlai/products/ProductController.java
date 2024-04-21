package apps.naturalsqlai.products;



import apps.naturalsqlai.ChatBotRequest;
import apps.naturalsqlai.ChatBotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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


    @PostMapping("/chat-with-inventory")
    public ChatBotResponse chatWithInventory(@RequestBody ChatBotRequest chatBotRequest) {


        try {
            String question = chatBotRequest.question();
            String query = productAiService.generateSqlQuery(question);
            String sqlJson = productAiService.getJsonData(query);
            String answer = productAiService.generateAnswer(question,query,sqlJson);
            return new ChatBotResponse(question, answer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Error processing chat request", e);

        }


    }


}
