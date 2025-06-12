package apps.llm.toolcalling.products;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    public List<Product> findProductByName(String name) {
        return productRepository.findAllByNameContainingIgnoreCase(name);
    }

}
