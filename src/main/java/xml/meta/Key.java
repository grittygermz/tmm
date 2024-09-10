package xml.meta;

import javax.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class Key {
    @XmlElement(name = "KeyName")
    private String keyName;
    @XmlElement(name = "KeyValue")
    private String keyValue;
}
