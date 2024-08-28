package xml.audit;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class DataFile {
    @XmlElement(name = "FileName")
    private String fileName;
    @XmlElement(name = "FileSize")
    private long fileSize;
}
