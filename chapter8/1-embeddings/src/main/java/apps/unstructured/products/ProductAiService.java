package apps.unstructured.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAiService {

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;
    private final JdbcTemplate jdbcTemplate;


    @SneakyThrows
    public String getJsonData(String sql) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(rows);

    }

    public String readFromClasspath(String filename) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new RuntimeException("No file named " + filename + " found in classpath");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from classpath", e);
        }
    }

    String generateSqlQuery(String question) {
        String context = null;

        context = readFromClasspath("generate-sql-query-prompt.txt");

        log.info(context);
        var messages = new ArrayList<Message>();
        messages.add(new SystemMessage(context));
        messages.add(new UserMessage(question));
        Prompt prompt = new Prompt(messages);


        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        // get the answer
        String answer = chatResponse.getResult().getOutput().getContent();
        return answer;
    }

    String generateAnswer(String question, String sqlQuery, String sqlJson) {


        String context = readFromClasspath("sql-to-natural-prompt.txt");
        String userMessage = "Question:  \n" + question +
                "sqlQuery: \n" + sqlQuery +
                "results: " + sqlJson;


        log.info("userMessage: " + userMessage);
        log.info("context: " + context);
        var messages = new ArrayList<Message>();
        messages.add(new SystemMessage(context));
        messages.add(new UserMessage(userMessage));
        Prompt prompt = new Prompt(messages);


        // call the chat client
        ChatResponse chatResponse = vertexAiGeminiChatModel.call(prompt);

        // get the answer
        String result = chatResponse.getResult().getOutput().getContent();
        log.info("result: " + result);
        return result;
    }

}
