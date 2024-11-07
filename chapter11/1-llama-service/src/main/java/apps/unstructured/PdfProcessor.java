package apps.unstructured;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfProcessor {

    private static final String PNG_FORMAT = "png";

    /**
     * Converts a PDF resource into a list of pages with their corresponding image resources
     *
     * @param resource The PDF resource to process
     * @return List of PdfPage objects containing page numbers and their image resources
     * @throws IOException if there's an error processing the PDF
     */
    public List<PdfPage> convertPdfToPages(@NonNull Resource resource) throws IOException {
        try (PDDocument pdDocument = loadPdfDocument(resource)) {
            PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
            return convertPdfPagesToImages(pdDocument, pdfRenderer);
        }
    }



    /**
     * Convert all PDF pages to images
     */
    private List<PdfPage> convertPdfPagesToImages(PDDocument pdDocument, PDFRenderer pdfRenderer) throws IOException {
        List<PdfPage> pages = new ArrayList<>();
        int numberOfPages = pdDocument.getNumberOfPages();

        for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++) {
            Resource pageImageResource = convertPdfPageToImageResource(pdfRenderer, pageIndex);
            pages.add(new PdfPage(pageIndex + 1, pageImageResource));
        }

        return pages;
    }
    /**
     * Convert a PDF page to an image resource
     */
    private Resource convertPdfPageToImageResource(PDFRenderer pdfRenderer, int pageIndex) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage pageImage = pdfRenderer.renderImage(pageIndex);
            ImageIO.write(pageImage, PNG_FORMAT, outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }
    }
    /**
     * Load a PDF document from a resource
     */
    @SneakyThrows
    private PDDocument loadPdfDocument(Resource resource) {
        PDFParser pdfParser = new PDFParser(
                new org.apache.pdfbox.io.RandomAccessReadBuffer(resource.getInputStream()));
        return pdfParser.parse();
    }
}