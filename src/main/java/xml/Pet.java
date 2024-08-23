package xml;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@XmlRootElement(name="pet")
@XmlAccessorType(XmlAccessType.NONE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Pet {
    @XmlAttribute(name = "shop")
    private String shop;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "age")
    private int age;
    //@XmlElementWrapper(name = "thepetchild")
    @XmlElement
    private PetChild petChild;

}
