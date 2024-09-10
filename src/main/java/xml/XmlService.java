package xml;

import lombok.extern.slf4j.Slf4j;
import xml.audit.AuditFile;
import xml.audit.DataFile;
import xml.meta.Record;
import xml.meta.*;
import xml.metaDTO.IndexKeys;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XmlService {
    private Marshaller metaMarshaller;
    private Marshaller auditMarshaller;
    //private MetaFile metaFile;
    //private AuditFile auditFile;

    private void initMarshallers() throws JAXBException {
        JAXBContext jaxbContext1 = JAXBContext.newInstance(MetaFile.class);
        metaMarshaller = jaxbContext1.createMarshaller();
        metaMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        JAXBContext jaxbContext2 = JAXBContext.newInstance(AuditFile.class);
        auditMarshaller = jaxbContext2.createMarshaller();
        auditMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }


    public XmlService() {
        try {
            initMarshallers();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO if file is large, is marshalling directly to file ok?
    //public void createXmlFile(FileType fileType, File outputFile) {
    //    try {
    //        switch (fileType) {
    //            case Meta:
    //                metaMarshaller.marshal(metaFile, outputFile);
    //                break;
    //            case Audit:
    //                auditMarshaller.marshal(auditFile, outputFile);
    //                break;
    //            default:
    //                throw new RuntimeException("xml file couldnt be created");
    //        }
    //    } catch (JAXBException e) {
    //        throw new RuntimeException(e);
    //    }
    //}

    public void createAuditFile(AuditFile auditFile, File outputFile) {
        try {
            auditMarshaller.marshal(auditFile, outputFile);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void createMetaFile(MetaFile metaFile, File outputFile) {
        try {
            metaMarshaller.marshal(metaFile, outputFile);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    //public void initMetaFile() {
    //    metaFile = new MetaFile(new ArrayList<>());
    //}

    public void addRecordToMetaFile(IndexKeys indexKeys, String filename, MetaFile metaFile) {
        List<Key> keys = new ArrayList<>();

        //TODO is there a more dynamic way to do this?
        keys.add(new Key("LDD", indexKeys.getLDD()));
        keys.add(new Key("BCNR", indexKeys.getBCNR()));
        keys.add(new Key("KUNDENNR", indexKeys.getKUNDENNR()));
        keys.add(new Key("GPN", indexKeys.getGPN()));
        keys.add(new Key("MESSAGE_ID", indexKeys.getMESSAGE_ID()));
        keys.add(new Key("DOCID", indexKeys.getDOCID()));
        keys.add(new Key("TYPEOFMAILING", indexKeys.getTYPEOFMAILING()));
        keys.add(new Key("TITLE", indexKeys.getTITLE()));
        keys.add(new Key("JOBID", indexKeys.getJOBID()));

        MetaData metaData = new MetaData(new Index(keys));
        Position position = new Position(filename);

        metaFile.getRecords().add(new Record(metaData, position));
    }

    //public void initAuditFile(Header header, UnitOfWork uow) {
    //    auditFile = new AuditFile(header, uow);
    //}

    // used for datafile to uow
    public void addUowDatafileContentsToAuditFile(DataFile dataFile, AuditFile auditFile) {
        auditFile.getUnitOfWork().getDataFile().add(dataFile);
    }

    // used for metafile to uow
    public void addUowMetafileContentsToAuditFile(DataFile metaFile, AuditFile auditFile) {
        auditFile.getUnitOfWork().setMetaFile(metaFile);
    }


}
