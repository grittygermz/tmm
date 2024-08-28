package xml.audit;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
public class Header {
    @XmlElement(name = "BusinessDate")
    private String businessDate;
    //TODO which time to use here?
    @XmlElement(name = "BusinessTime")
    private String businessTime;
    @XmlElement(name = "SubmissionSequenceNr")
    private int submissionSequenceNr;
    @XmlElement(name = "SubmissionVersionNr")
    private int submissionVersionNr;
    @XmlElement(name = "AdHoc")
    private String adHoc = "N";

    public Header(String businessDate, String businessTime, int submissionSequenceNr, int submissionVersionNr) {
        this.businessDate = businessDate;
        this.businessTime = businessTime;
        this.submissionSequenceNr = submissionSequenceNr;
        this.submissionVersionNr = submissionVersionNr;
    }
}
