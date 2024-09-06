package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class JobInput {
    private String archiveFilesPath;
    private String docType;
    private String archiveId;
    private String bookingCenter;
    private String date;
    private int seqNumStart;
    private int seqNumEnd;
    private int versionNum;
    private int maxFolderSizeGB;
    private int maxFolderSizeMB;
    private int maxFolderSizeKB;
    private int maxDataFiles;
    @JsonProperty("gpn")
    private String GPN;
    @JsonProperty("message_id")
    private String MESSAGE_ID;

    private String docId;
    private String typeOfMailing;
    private String title;
    private String jobId;
}
