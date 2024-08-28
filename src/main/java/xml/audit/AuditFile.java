package xml.audit;

import jakarta.xml.bind.annotation.*;
import lombok.*;
import xml.meta.Record;

import java.util.List;

@XmlRootElement(name = "AuditFile")
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
public class AuditFile {
    @XmlAttribute(name = "xmlns:xsi")
    private String nsXsi = "http://www.w3.org/2001/XMLSchema-instance";
    @XmlAttribute(name = "xsi:noNamespaceSchemaLocation")
    private String noNamespaceSchemaLocation = "..\\Schemata\\Audit_V2.0.xsd";
    @XmlAttribute(name = "Version")
    private String version = "1";

    @XmlElement(name = "Header")
    private Header header;

    @XmlElement(name = "UnitOfWork")
    @Getter
    private UnitOfWork unitOfWork;

    public AuditFile(Header header, UnitOfWork unitOfWork) {
        this.header = header;
        this.unitOfWork = unitOfWork;
    }
}
