package xml.meta;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.NoArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
public class Technical {
    @XmlElement(name = "MimeType")
    private String mimeType = "PDF";
    @XmlElement(name = "MimeTypeVersion")
    private String mimeTypeVersion = "1.0";
}
