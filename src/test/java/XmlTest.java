import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import json.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xml.MyDate;
import xml.Pet;
import xml.PetChild;
import xml.PetChildChild;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class XmlTest {

    //@Test
    void addAndReadFileMetadata() throws IOException {
        Path path = new File("myfile.txt").toPath();

        boolean b = Files.getFileStore(path).supportsFileAttributeView(UserDefinedFileAttributeView.class);
        System.out.println(b);

        UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        view.write("user.ginro", Charset.defaultCharset().encode("testtest"));


        String name = "user.ginro";
        ByteBuffer buf = ByteBuffer.allocate(view.size(name));
        view.read(name, buf);
        buf.flip();
        String value = Charset.defaultCharset().decode(buf).toString();
        System.out.println(value);

    }

    //@Test
    void addMetadataPDFFiles() throws IOException {
        File file = new File("sample.pdf");
        PDDocument document = PDDocument.load(file);
        ;

        PDDocumentInformation documentInformation = document.getDocumentInformation();
        documentInformation.setCustomMetadataValue("pet", "ginro");
        document.save("modifiedSample.pdf");
        document.close();

    }

    //https://www.tutorialspoint.com/pdfbox/pdfbox_document_properties.htm
    //https://pdfbox.apache.org/docs/2.0.13/javadocs/org/apache/pdfbox/pdmodel/PDDocumentInformation.html
    @Test
    void viewMetadataPDFFiles() throws IOException {
        File fileM = new File("modifiedSample.pdf");
        PDDocument documentM = PDDocument.load(fileM);
        PDDocumentInformation documentInformationM = documentM.getDocumentInformation();
        String petM = documentInformationM.getCustomMetadataValue("pet");
        System.out.println(petM);
        documentM.close();

        File file = new File("ubs.pdf");
        PDDocument document = PDDocument.load(file);
        ;

        PDDocumentInformation documentInformation = document.getDocumentInformation();
        Set<String> metadataKeys = documentInformation.getMetadataKeys();

        for (String k : metadataKeys) {
            System.out.println(k);
        }
        String techead = documentInformation.getCustomMetadataValue("techead");
        System.out.println(techead);
        document.close();
    }


    // using jaxb directly
    @Test
    void handleXMLDocs1() throws JAXBException {
        PetChildChild pcc1 = new PetChildChild(
                "chewy1",
                "sally toy",
                4,
                LocalDateTime.now(),
                new MyDate(LocalDateTime.now()));
        PetChildChild pcc2 = new PetChildChild("chewy2",
                "elephant",
                2,
                LocalDateTime.now().minusHours(3),
                new MyDate(LocalDateTime.now().minusMinutes(20)));

        List<PetChildChild> pccList = new ArrayList<>();
        pccList.add(pcc1);
        pccList.add(pcc2);

        PetChild petChild = new PetChild(pccList);

        JAXBContext jaxbContext = JAXBContext.newInstance(Pet.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(new Pet("cing-de-pom", "ginro", 3, petChild), new File("./test1.xml"));
    }

    @Test
    void jsonToPojo() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{ \"brand\" : \"toyota\", \"age\" : 5 }";
        Vehicle vehicle = objectMapper.readValue(json, Vehicle.class);
        System.out.println(vehicle);

        //Car car = objectMapper.readValue(new File("src/test/resources/json_car.json"), Car.class);

    }

    //pojo to json
    @Test
    void pojoToJson() {
        //objectMapper.writeValue(new File("target/car.json"), car);
    }

    //@Test
    //void compute() {
    //    int MAXFOLDERSIZE = 10;
    //    int MAXNUMBEROFFILESINFOLDER = 3;
    //    Map<String, Integer> filesizeMap = new HashMap<>();
    //    ArrayList<Map<String, Integer>> zipFolders = new ArrayList<>();
    //    zipFolders.add(new HashMap<>());
    //
    //    filesizeMap.put("0.pdf", 3);
    //    filesizeMap.put("1.pdf", 6);
    //    filesizeMap.put("2.pdf", 7);
    //    filesizeMap.put("3.pdf", 2);
    //    filesizeMap.put("4.pdf", 1);
    //    filesizeMap.put("5.pdf", 6);
    //    filesizeMap.put("6.pdf", 3);
    //    filesizeMap.put("7.pdf", 4);
    //    filesizeMap.put("8.pdf", 5);
    //    filesizeMap.put("9.pdf", 4);
    //    filesizeMap.put("10.pdf", 2);
    //    filesizeMap.put("11.pdf", 3);
    //    filesizeMap.put("12.pdf", 4);
    //    filesizeMap.put("13.pdf", 5);
    //
    //    LinkedHashMap<String, Integer> collect = filesizeMap.entrySet().stream()
    //            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(
    //                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    //    System.out.println(collect);
    //
    //    for (Map.Entry<String, Integer> entry : collect.entrySet()) {
    //        for(Map<String, Integer> folder: zipFolders) {
    //            if(folder.size() >= MAXNUMBEROFFILESINFOLDER) {
    //                continue;
    //            }
    //            folder.compute()
    //
    //        }
    //
    //    }
    //
    //    //Map<String, Integer> sortedfilezieMap = sortByValue(filesizeMap);
    //    //System.out.println(sortedfilezieMap);
    //}


}
