package xml.meta;

import javax.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class Record {
    @XmlElement(name = "MetaData")
    private MetaData metaData;
    @XmlElement(name = "Position")
    private Position position;
}
