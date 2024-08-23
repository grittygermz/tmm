package xml;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
public class PetChildChild {
    @XmlElement(name = "chewies")
    private String chewies;
    @XmlAttribute(name="toys")
    private String toys;
    //@XmlTransient
    @XmlElement(name = "count")
    private Integer count;
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdTime;
    @XmlJavaTypeAdapter(MyDateAdapter.class)
    private MyDate dateOnly;

}
