package xml.meta;

import javax.xml.bind.annotation.*;
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
