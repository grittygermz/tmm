package xml.audit;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

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
