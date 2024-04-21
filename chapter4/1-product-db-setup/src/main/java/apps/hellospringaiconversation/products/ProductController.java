package apps.hellospringaiconversation.products;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;



    @GetMapping("/{id}")
    public Optional<Product> getProductById(@PathVariable("id") String productId) {
       return productRepository.findById(productId);

    }





}
