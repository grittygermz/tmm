package compute;

import compute.model.PDFMetadata;
import json.JobInput;

public class InputValidator {

    private static boolean hasMaxLength(String field, int maxLength){
        return field.length() <= maxLength;
    }

    public static boolean validate(JobInput jobInput) {
        return hasMaxLength(jobInput.getJobId(), 64) &&
                hasMaxLength(jobInput.getDocId() , 16) &&
                hasMaxLength(jobInput.getTitle(), 254) &&
                hasMaxLength(jobInput.getTypeOfMailing(), 254);
    }

    public static boolean validatePDFMetadata(PDFMetadata pdfMetadata) {
        return hasMaxLength(pdfMetadata.getBcnr(), 4) &&
                hasMaxLength(pdfMetadata.getKudennr(), 8) &&
                hasMaxLength(pdfMetadata.getLdd(), 8);

    }
}
