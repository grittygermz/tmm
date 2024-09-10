package xml.meta;

import javax.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@XmlRootElement(name = "MetaFile")
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
public class MetaFile {
    @XmlAttribute(name = "xmlns:xsi")
    private String nsXsi = "http://www.w3.org/2001/XMLSchema-instance";
    @XmlAttribute(name = "xsi:noNamespaceSchemaLocation")
    private String noNamespaceSchemaLocation = "../schemas/Metafile_v2.0.xsd";
    @XmlAttribute(name = "Version")
    private String version = "2.0";

    @Getter
    @XmlElement(name = "Record")
    private List<Record> records;

    public MetaFile(List<Record> records) {
        this.records = records;
    }

}
