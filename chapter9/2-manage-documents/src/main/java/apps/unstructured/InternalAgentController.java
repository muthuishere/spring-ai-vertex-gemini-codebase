package apps.unstructured;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/agent")
@Slf4j
public class InternalAgentController {
//       TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
//        documents = tokenTextSplitter.split(documents);
    private List<Document> readDocumentsWithTika(Resource resource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        List<Document> documents = tikaDocumentReader.read();
        return documents;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadFile(@RequestPart MultipartFile file) {

        Resource resource = file.getResource();
        readDocumentsWithTika(resource);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
