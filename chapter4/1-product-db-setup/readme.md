### Project Setup

1. Clone the repository.
2. Utilize the [ecommerce_mock_data.sql](ecommerce_mock_data.sql) file to generate the database and tables.

### Configuring GCP Cloud SQL

- Modify the `src/main/resources/application.properties` file with your connection details:

```properties
spring.ai.vertex.ai.gemini.projectId=your_project_id
spring.ai.vertex.ai.gemini.location=us-central1
spring.ai.vertex.ai.gemini.chat.options.model=gemini-1.5-flash
spring.cloud.gcp.sql.enabled=true
spring.cloud.gcp.sql.instance-connection-name=your_project_id:us-central1:postgres-instance
spring.cloud.gcp.sql.database-name=postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect


```
### Configuring Local Postgres
Update the src/main/resources/application.properties file with the following properties:

```properties

spring.ai.vertex.ai.gemini.projectId=your_project_id
spring.ai.vertex.ai.gemini.location=us-central1
spring.ai.vertex.ai.gemini.chat.options.model=gemini-1.5-flash
spring.cloud.gcp.sql.enabled=false
spring.cloud.gcp.sql.instance-connection-name=your_project_id:us-central1:postgres-instance
spring.cloud.gcp.sql.database-name=postgres
spring.datasource.username=postgres
spring.datasource.password=postgres

```

After launching the application, execute the tests from the requests/productsapi.http file
