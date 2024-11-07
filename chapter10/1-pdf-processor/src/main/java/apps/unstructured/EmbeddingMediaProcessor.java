package apps.unstructured;

import com.google.cloud.storage.Blob;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingMediaProcessor {



    private final StorageBucketService storageBucketService;
    private final PdfProcessor pdfProcessor;


    @SneakyThrows
    public List<Document> processPdfToDocuments(@NonNull Resource resource,
                                                @NonNull Map<String, Object> metadata){


        List<PdfPage> pdfPages= pdfProcessor.convertPdfToPages(resource);

        return pdfPages.stream()
                .map(pdfPage -> createPageDocument(pdfPage, metadata))
                .collect(Collectors.toList());
    }



    @SneakyThrows
    private Document createPageDocument(PdfPage pdfPage, Map<String, Object> baseMetadata) {


        int pageNumber = pdfPage.getPageNumber();
        String actualFilename = baseMetadata.get("filename").toString();
        String pageFilename = "page" + pageNumber + actualFilename;


        Resource imageResource = pdfPage.getPageResource();
        byte[] bytes = imageResource.getContentAsByteArray();
        String fileId= storageBucketService.uploadFile(pageFilename, bytes);


        Map<String, Object> pageMetadata = new HashMap<>(baseMetadata);
        pageMetadata.put("page", pageNumber);
        pageMetadata.put("mimeType", MimeTypeUtils.IMAGE_PNG_VALUE);
        pageMetadata.put("pageImageId", fileId);

        return Document.builder()
                .withContent(pageFilename)
                .withMedia(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
                .withMetadata(pageMetadata)
                .build();
    }



    public Media getMedia(Document document) {
        String pageImageId = null;
        String mimeType = null;
        mimeType = document.getMetadata().get("mimeType").toString();
        pageImageId = document.getMetadata().get("pageImageId").toString();

        Blob blob = storageBucketService.getFile(pageImageId);
        byte[] content = blob.getContent();
        ByteArrayResource byteArrayResource = new ByteArrayResource(content);
        MimeType mimeTypeFromExtension = MimeTypeUtils.parseMimeType(mimeType);
        return new Media(mimeTypeFromExtension, byteArrayResource)
                ;
    }


}