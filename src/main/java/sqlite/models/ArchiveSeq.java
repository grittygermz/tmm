package sqlite.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@DatabaseTable
@Data
@NoArgsConstructor
@ToString
public class ArchiveSeq {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private int seqNum;
    @DatabaseField
    private int numDataFiles;
    @DatabaseField
    private long folderSize;

    @DatabaseField(foreign = true)
    private ArchivePack archivePack;

    public ArchiveSeq(int seqNum, int numDataFiles, long folderSize, ArchivePack archivePack) {
        this.seqNum = seqNum;
        this.numDataFiles = numDataFiles;
        this.folderSize = folderSize;
        this.archivePack = archivePack;
    }
}
