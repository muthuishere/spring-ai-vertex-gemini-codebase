package apps.unstructured.products;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


public class TestContainerIntegrationTest {

    static Logger log = LoggerFactory.getLogger(TestContainerIntegrationTest.class);
    static PostgreSQLContainer<?> postgres;


    static {
        createPostgresContainer();

    }


    static void createPostgresContainer() {

        log.info("Starting container");

        DockerImageName postgresVectorImage
                = DockerImageName.parse("ankane/pgvector:latest").asCompatibleSubstituteFor("postgres");
        postgres = new PostgreSQLContainer<>(postgresVectorImage);

        postgres
                .withInitScript("ecommerce_mock_data.sql");

        postgres.start();
        Runtime.getRuntime().addShutdownHook(new Thread(TestContainerIntegrationTest::stopPostgresContainer));

    }

    static void stopPostgresContainer() {
        log.info("Stopping container");
        postgres.stop();
    }


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

}
