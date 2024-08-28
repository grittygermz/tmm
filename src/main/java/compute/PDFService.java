package compute;

import compute.model.PDFMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class PDFService {
    public PDFMetadata extractPDFMetadata(File pdfFile) {
        PDFMetadata pdfMetadata;
        try(PDDocument pdfDoc = PDDocument.load(pdfFile)) {
            PDDocumentInformation di = pdfDoc.getDocumentInformation();
            pdfMetadata = new PDFMetadata(transformLDD(di.getCustomMetadataValue("LDD")),
                    di.getCustomMetadataValue("KUNDENNR"),
                    di.getCustomMetadataValue("BCNR"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.debug("extracted metadata from pdf {}", pdfMetadata);

        return pdfMetadata;
    }

    private String transformLDD(String LDD) {
        DateTimeFormatter inputDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.from(inputDateFormatter.parse(LDD));

        return localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
