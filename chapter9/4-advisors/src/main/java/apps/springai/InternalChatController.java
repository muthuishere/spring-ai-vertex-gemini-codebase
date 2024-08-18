package apps.springai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InternalChatController {


    final VectorStore vectorStore;

    final JdbcTemplate jdbcTemplate;
    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;


    //   .system("Answer in a casual tone and provide the information requested based on the context")

    @PostMapping("/api/internaldata/chat")
    public ChatBotResponse answerQuestion(@RequestBody SimilaritySearchRequest similaritySearchRequest) {

        Filter.Expression expression = null;

        if(null != similaritySearchRequest.department()){
            expression = new FilterExpressionBuilder()
                    .eq("department", similaritySearchRequest.department())
                    .build();
        }

        String question = similaritySearchRequest.question();
        SearchRequest searchRequest = SearchRequest
                .query(question)
                .withFilterExpression(expression)
                .withTopK(similaritySearchRequest.limit())
                .withSimilarityThreshold(similaritySearchRequest.getThreshold());

        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore, searchRequest);

        var chatClientRequest = ChatClient.builder(vertexAiGeminiChatModel).build()
                .prompt()
                .advisors(questionAnswerAdvisor)
                .user(question);


        ChatResponse chatResponse = chatClientRequest
                                        .call()
                                        .chatResponse();

        String answer = chatResponse.getResult().getOutput().getContent();

        return new ChatBotResponse(question, answer);

    }




}