package apps.springai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public  class DocumentRowMapper implements RowMapper<Document> {
    private static final String COLUMN_EMBEDDING = "embedding";
    private static final String COLUMN_METADATA = "metadata";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CONTENT = "content";

    private final ObjectMapper objectMapper;

    public DocumentRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("id");
        String content = rs.getString("content");
        PGobject pgMetadata = (PGobject)rs.getObject("metadata", PGobject.class);
        PGobject embedding = (PGobject)rs.getObject("embedding", PGobject.class);

        Map<String, Object> metadata = this.toMap(pgMetadata);

        Document document = new Document(id, content, metadata);
        document.setEmbedding(this.toDoubleList(embedding));
        return document;
    }

    private List<Double> toDoubleList(PGobject embedding) throws SQLException {
        float[] floatArray = (new PGvector(embedding.getValue())).toArray();
        return IntStream.range(0, floatArray.length).mapToDouble((i) -> {
            return (double)floatArray[i];
        }).boxed().toList();
    }

    private Map<String, Object> toMap(PGobject pgObject) {
        String source = pgObject.getValue();

        try {
            return (Map)this.objectMapper.readValue(source, Map.class);
        } catch (JsonProcessingException var4) {
            throw new RuntimeException(var4);
        }
    }
}