# Natural SQL AI Application

This application is a Spring Boot project that uses AI to generate SQL queries from natural language questions and provide answers in a human-readable format. The application is written in Java and uses the Spring Boot framework, SQL for database interactions, and Gradle for build and dependency management.

## Project Structure

The project is structured into several packages and files:

- `ProductAiService.java`: This service class is responsible for generating SQL queries from natural language questions and providing answers in a human-readable format.

- `ProductController.java`: This controller class handles HTTP requests and responses. It uses the `ProductAiService` to process the requests.

- `ProductAiServiceIntegrationTest.java`: This class contains integration tests for the `ProductAiService`.

- `TestContainerIntegrationTest.java`: This class sets up a PostgreSQL test container for integration tests.

- `build.gradle`: This file contains the project's build configuration and dependencies.

- `requests/chat-with-inventory-tests.http`: This file contains HTTP tests for the `/api/products/chat-with-inventory` endpoint.

## Running Tests

The project includes a suite of tests that can be run to verify the functionality of the application. These tests are located in the `ProductAiServiceIntegrationTest.java` file and can be run using the following command:

```bash
./gradlew test
```

## HTTP Tests

The `requests/chat-with-inventory-tests.http` file contains HTTP tests for the `/api/products/chat-with-inventory` endpoint. These tests check for different scenarios and responses from the endpoint.

## Building and Running the Application

To build the application, use the following command:

```bash
./gradlew build
```

To run the application, use the following command:

```bash
./gradlew bootRun
```

## Dependencies

The project uses several dependencies, including:

- Spring Boot for the application framework
- Google Cloud's Spring Cloud GCP Starter for Google Cloud Platform integration
- Spring AI's Vertex AI Gemini Spring Boot Starter for AI capabilities
- PostgreSQL for the database
- Testcontainers for integration testing

For a full list of dependencies, refer to the `build.gradle` file.
.