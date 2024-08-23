package xml;

import jakarta.xml.bind.annotation.adapters.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyDateAdapter extends XmlAdapter<String, MyDate> {

    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    @Override
    public MyDate unmarshal(String s) throws Exception {
        return null;
    }

    @Override
    public String marshal(MyDate myDate) throws Exception {
        return myDate.getDate().format(dateFormat);
    }
}
