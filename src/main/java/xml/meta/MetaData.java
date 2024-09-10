package xml.meta;

import javax.xml.bind.annotation.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MetaData {
    @XmlElement(name = "Index")
    private Index index;
    @XmlElement(name = "Technical")
    private Technical technical = new Technical();

    public MetaData(Index index) {
        this.index = index;
    }
}
