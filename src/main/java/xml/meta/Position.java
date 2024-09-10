package xml.meta;

import javax.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    @XmlElement(name = "FileName")
    private String fileName;
}
