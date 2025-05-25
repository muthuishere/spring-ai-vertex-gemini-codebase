package apps.unstructured.modelgarden;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class ModelGardenConfig {

    @Bean
    public RestClient.Builder createRestClientBuilder() {

        return RestClient.builder()
                .requestInterceptor(new GcpModelGardenInterceptor());
    }


}
