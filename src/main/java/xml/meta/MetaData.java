package xml.meta;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
