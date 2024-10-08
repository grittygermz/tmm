package xml.audit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
public class UnitOfWork {
    @XmlAttribute(name = "UnitSequenceNr")
    private int unitSequenceNr = 1;
    @XmlAttribute(name = "ArchiveID")
    private String archiveID;
    @XmlAttribute(name = "FileCount")
    private int fileCount;

    @Getter
    @XmlElement(name = "DataFile")
    private List<DataFile> dataFile = new ArrayList<>();
    @Setter
    @XmlElement(name = "MetaFile")
    private DataFile metaFile;

    public UnitOfWork(String archiveID, int fileCount) {
        this.archiveID = archiveID;
        this.fileCount = fileCount;
    }
}
