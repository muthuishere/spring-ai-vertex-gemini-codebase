package apps.unstructured;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class PdfPage {
    private int pageNumber;
    private Resource pageResource;
}
