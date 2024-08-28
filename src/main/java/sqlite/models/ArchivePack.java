package sqlite.models;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;

import java.util.Date;

@DatabaseTable
@Data
@NoArgsConstructor
@ToString
public class ArchivePack {
    @DatabaseField(generatedId = true)
    private long packId;

    // combination of docType + archiveId + bookingCenter + date + versionNum
    @DatabaseField
    private String packPrefix;
    @DatabaseField
    private int seqStart;
    @DatabaseField
    private int seqEnd;
    @DatabaseField
    private Date startTime;
    @DatabaseField
    private Date endTime;


    public ArchivePack(String packPrefix, int seqStart, Date startTime) {
        this.packPrefix = packPrefix;
        this.seqStart = seqStart;
        this.startTime = startTime;
    }
}
