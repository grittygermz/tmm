package xmlSample;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name="petchildz")
@XmlAccessorType(XmlAccessType.NONE)
public class PetChild {
    @XmlElement
    List<PetChildChild> petchildchild;
}
