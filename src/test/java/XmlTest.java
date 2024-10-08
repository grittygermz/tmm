import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import compute.model.ArchiveFolder;
import json.JobInput;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.jupiter.api.Test;
import sqlite.models.ArchivePack;
import sqlite.models.ArchiveSeq;
import xml.audit.AuditFile;
import xml.audit.DataFile;
import xml.audit.Header;
import xml.audit.UnitOfWork;
import xml.meta.Record;
import xml.meta.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    void handleXMLDocs2() throws JAXBException {

        Key key = new Key("LDD", "ginro");
        Key key1 = new Key("BCNR", "haihai");
        List<Key> keys = new ArrayList<>();
        keys.add(key);
        keys.add(key1);

        Position position = new Position("abcd.pdf");
        MetaData metaData = new MetaData(new Index(keys));
        Record record = new Record(metaData, position);

        List<Record> records = new ArrayList<>();
        records.add(record);
        records.add(record);
        MetaFile metaFile = new MetaFile(records);


        JAXBContext jaxbContext = JAXBContext.newInstance(MetaFile.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(metaFile, new File("./test2.xml"));
    }

    @Test
    void handleXMLDocs3() throws JAXBException {

        Header header = new Header("20230427", "075056", 1, 1);
        DataFile d1 = new DataFile("file1.data.pdf", 111);
        DataFile d2 = new DataFile("file2.data.pdf", 222);

        DataFile meta = new DataFile("file2.meta.xml", 333);


        UnitOfWork uow = new UnitOfWork("ABCD", 5);
        uow.getDataFile().add(d1);
        uow.getDataFile().add(d2);
        uow.setMetaFile(meta);
        Object auditFile = new AuditFile(header, uow);


        JAXBContext jaxbContext = JAXBContext.newInstance(AuditFile.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(auditFile, new File("./test3.xml"));
    }


    //@Test
    //void jsonToPojoPOC() throws JsonProcessingException {
    //    ObjectMapper objectMapper = new ObjectMapper();
    //    String json = "{ \"brand\" : \"toyota\", \"age\" : 5 }";
    //    Vehicle vehicle = objectMapper.readValue(json, Vehicle.class);
    //    System.out.println(vehicle);
    //
    //    //Car car = objectMapper.readValue(new File("src/test/resources/json_car.json"), Car.class);
    //}

    @Test
    void jsonToPojo() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        //String json = "{ \"brand\" : \"toyota\", \"age\" : 5 }";
        //Vehicle vehicle = objectMapper.readValue(json, Vehicle.class);

        JobInput jobInput = objectMapper.readValue(new File("src/test/resources/jobInput.json"), JobInput.class);
        System.out.println(jobInput);

    }

    //pojo to json
    @Test
    void pojoToJson() {
        //objectMapper.writeValue(new File("target/car.json"), car);
    }

    @Test
    void compute() {
        int MAXFOLDERSIZE = 10;
        int MAXNUMBEROFFILESINFOLDER = 3;
        Map<String, Long> filesizeMap = new HashMap<>();
        List<ArchiveFolder> zipFolders = new ArrayList<>();
        zipFolders.add(new ArchiveFolder());

        filesizeMap.put("0.pdf", 3L);
        filesizeMap.put("1.pdf", 6L);
        filesizeMap.put("2.pdf", 7L);
        filesizeMap.put("3.pdf", 2L);
        filesizeMap.put("4.pdf", 1L);
        filesizeMap.put("5.pdf", 6L);
        filesizeMap.put("6.pdf", 3L);
        filesizeMap.put("7.pdf", 4L);
        filesizeMap.put("8.pdf", 5L);
        filesizeMap.put("9.pdf", 4L);
        filesizeMap.put("10.pdf", 2L);
        filesizeMap.put("11.pdf", 3L);
        filesizeMap.put("12.pdf", 4L);
        filesizeMap.put("13.pdf", 5L);

        // sort such that largest is first and smallest is last
        LinkedHashMap<String, Long> collect = filesizeMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println(collect);

        for (Map.Entry<String, Long> entry : collect.entrySet()) {
            boolean wasAddedToFolder = false;
            for (ArchiveFolder folder : zipFolders) {
                if (folder.getFileNameSizeMap().size() >= MAXNUMBEROFFILESINFOLDER ||
                        folder.getFolderSize() > MAXFOLDERSIZE) {
                    continue;
                }
                long newFolderSize = folder.getFolderSize() + entry.getValue();
                if (newFolderSize > MAXFOLDERSIZE) {
                    continue;
                }
                folder.getFileNameSizeMap().put(entry.getKey(), entry.getValue());
                folder.setFolderSize(newFolderSize);
                wasAddedToFolder = true;
            }
            if (!wasAddedToFolder) {
                ArchiveFolder archiveFolder = new ArchiveFolder();
                archiveFolder.getFileNameSizeMap().put(entry.getKey(), entry.getValue());
                archiveFolder.setFolderSize(entry.getValue());
                zipFolders.add(archiveFolder);
            }
        }

        for (ArchiveFolder folder : zipFolders) {
            System.out.println(folder);
        }

        //Map<String, Integer> sortedfilezieMap = sortByValue(filesizeMap);
        //System.out.println(sortedfilezieMap);
    }

    //@Test
    void fileops() throws IOException {
        String src = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc";

        String dest = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfdest";
        Files.createDirectory(Paths.get(dest));

        Stream<Path> list = Files.list(Paths.get(src));

        AtomicInteger count = new AtomicInteger();
        list.forEach(file -> {
            try {
                count.getAndIncrement();
                Files.move(file, Paths.get(dest, String.format("%s.pdf", count)), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    //@Test
    void createInputMap() throws IOException {
        Map<String, Long> fileNameAndSizeMap = new HashMap<>();
        String src = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc";
        Stream<Path> list = Files.list(Paths.get(src));

        list.forEach(file -> {
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                fileNameAndSizeMap.put(file.toString(), basicFileAttributes.size());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(fileNameAndSizeMap);
    }

    @Test
    void writeToSqliteDB() {
        // this uses h2 by default but change to match your database
        String databaseUrl = "jdbc:sqlite:sample.db";
        // create a connection source to our database
        try (ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl)) {

            // instantiate the dao
            Dao<ArchivePack, String> archivePackDao =
                    DaoManager.createDao(connectionSource, ArchivePack.class);
            Dao<ArchiveSeq, String> archiveSeqDao =
                    DaoManager.createDao(connectionSource, ArchiveSeq.class);

            // if you need to create the 'accounts' table make this call
            TableUtils.dropTable(connectionSource, ArchivePack.class, true);
            TableUtils.createTable(connectionSource, ArchivePack.class);

            TableUtils.dropTable(connectionSource, ArchiveSeq.class, true);
            TableUtils.createTable(connectionSource, ArchiveSeq.class);


            // create an instance of Account
            ArchivePack archivePack = new ArchivePack("thePrefix", 1, new Date());

            // persist the account object to the database
            archivePackDao.create(archivePack);

            // retrieve the account from the database by its id field (name)
            ArchivePack archivePack1 = archivePackDao.queryForFirst();
            System.out.println(archivePack1.getPackId());
            ArchiveSeq archiveSeq = new ArchiveSeq(10, 200, 50, archivePack1);

            archiveSeqDao.create(archiveSeq);
            archiveSeqDao.create(new ArchiveSeq(12, 200, 50, archivePack1));

            try (GenericRawResults<String[]> rawResults = archiveSeqDao.queryRaw("select * from archiveseq where seqNum = ?", "10")) {
                List<String[]> results = rawResults.getResults();

                for (String s : rawResults.getColumnNames()) {
                    System.out.println(s);
                }

                String[] strings = results.get(0);
                for (String s : strings) {
                    System.out.println(s);
                }


                //archiveSeqDao.update()
            }

            //archiveSeqDao.queryRaw("select * from archiveseq where seqNum = ?",
            //        new RawRowMapper<ArchiveSeq>() {
            //
            //            @Override
            //            public ArchiveSeq mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
            //                return new ArchiveSeq(Integer.parseInt(resultColumns[1]), resultColumns[4]);
            //            }
            //        }, "10")
            QueryBuilder<ArchiveSeq, String> archiveSeqStringQueryBuilder = archiveSeqDao.queryBuilder();
            archiveSeqStringQueryBuilder.where().eq("seqNum", 10);
            List<ArchiveSeq> query = archiveSeqDao.query(archiveSeqStringQueryBuilder.prepare());
            ArchiveSeq archiveSeq1 = query.get(0);
            System.out.println(archiveSeq1);

            archiveSeq1.setNumDataFiles(2000);
            archiveSeq1.setFolderSize(999);
            archiveSeqDao.update(archiveSeq1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    void stringformat() {
        int a = 1;
        String s = String.valueOf(a);
        String format = String.format("%03d", a);
        System.out.println(format);

        //System.out.println(a--);
        System.out.println(--a);
    }

    @Test
    void zipDirectory() {
        String path = "src/main/resources/AK3MMC01.BDCZ.CH..20240826.S003.V1";
        File dir = new File(path);
        String zipDirName = "src/main/resources/abc.zip";
        try {
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(zipDirName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            Files.list(Paths.get(path))
                    .forEach(f -> {
                        String filePath = f.toAbsolutePath().toString();
                        System.out.println("Zipping " + filePath);
                        //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                        ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length() + 1, filePath.length()));
                        try {
                            zos.putNextEntry(ze);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        //read the file and write to ZipOutputStream
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(filePath);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        byte[] buffer = new byte[1024];
                        int len;
                        while (true) {
                            try {
                                if (!((len = fis.read(buffer)) > 0)) break;
                                zos.write(buffer, 0, len);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        try {
                            zos.closeEntry();
                            fis.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@Test
    void zip4jtest() throws IOException {
        String path = "src/main/resources/AK3MMC01.BDCZ.CH..20240826.S004.V1";
        File dir = new File(path);
        String zipDirName = "src/main/resources/abc.zip";

        Files.list(Paths.get(path)).forEach(pathz -> {
            try {
                new ZipFile(zipDirName).addFile(pathz.toFile());
            } catch (ZipException e) {
                throw new RuntimeException(e);
            }
        });

        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(f -> {
                        try {
                            Files.delete(f);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    //@Test
    void callZipParallelAcrossMultipleFolders() {
        List<List<String>> jobs = new ArrayList<>();
        List<String> folders = new ArrayList<>();
        String src = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc";
        String dest = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc.zip";
        folders.add(src);
        folders.add(dest);
        String src1 = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc1";
        String dest1 = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc1.zip";
        List<String> folders1 = new ArrayList<>();
        folders1.add(src1);
        folders1.add(dest1);
        jobs.add(folders);
        jobs.add(folders1);

        jobs.stream().map(j -> CompletableFuture.runAsync(() -> {
                    try {
                        zipParallel(j.get(0), j.get(1));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })).map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    //@Test
    void callZipParallel() throws IOException {
        String src = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc";
        String dest = "C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\src\\main\\resources\\pdfsrc.zip";
        zipParallel(src, dest);
    }


    private void zipParallel(String sourceFolder, String zipFilePath) throws IOException {
        log.info("starting zip");
        ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();

        //OutputStream outputStream = null;
        // = null;
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(zipFilePath));
             ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {

            //File srcFolder = new File(sourceFolder);
            //if (srcFolder.isDirectory()) {
            // uncomment following code if you want to add all files under srcFolder
            //Iterator<File> fileIterator = Arrays.asList(srcFolder.listFiles()).iterator();


            //File zipFile = new File(zipFilePath);
            //zipFile.delete();
            zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);

            int srcFolderLength = Paths.get(sourceFolder)
                    .toAbsolutePath().toString().length() + 1;  // +1 to remove the last file separator

            //while (fileIterator.hasNext()) {
            //    File file = fileIterator.next();

            // uncomment following code if you want to add all files under srcFolder
            //if (file.isDirectory()) {
            //        continue;
            //    }

            try (Stream<Path> files = Files.list(Paths.get(sourceFolder))) {
                files.forEach(file -> {
                    String relativePath = file.toAbsolutePath().toString()
                            .substring(srcFolderLength);

                    InputStreamSupplier streamSupplier = () -> {
                        InputStream is = null;
                        try {
                            is = Files.newInputStream(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return is;
                    };
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(relativePath);
                    zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                    scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
                });
            }
            scatterZipCreator.writeTo(zipArchiveOutputStream);
            zipArchiveOutputStream.close();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //@Test
    void availableProc() {
        int i = Runtime.getRuntime().availableProcessors();
        System.out.println(i);
    }

    @Test
    void checkheapsize() {
        long l = Runtime.getRuntime().maxMemory();
        System.out.println(l);
        //4 242 538 496
    }

    //@Test
    void doUntilOOM() {
        long l = Runtime.getRuntime().maxMemory();
        System.out.println(l);
        //4 242 538 496
        System.out.println("start..");

        long count = 0;
        MetaFile metaFile = new MetaFile(new ArrayList<>());
        while (true) {
            try {
                metaFile.getRecords().add(createRecordForMeta());
                count++;
            } catch (OutOfMemoryError e) {
                break;
            }
        }
        long l1 = Runtime.getRuntime().freeMemory();
        long l2 = Runtime.getRuntime().maxMemory();
        System.out.println(l1 + " " + l2);
        System.out.println("number of objects created before OOM " + count);
    }

    private Record createRecordForMeta() {
        Key key = new Key("LDD", "ginro");
        Key key1 = new Key("BCNR", "haihai");
        Key key2 = new Key("BCNR1", "haihai");
        Key key3 = new Key("BCNR2", "haihai");
        Key key4 = new Key("BCNR3", "haihai");
        Key key5 = new Key("BCNR4", "haihai");
        Key key6 = new Key("BCNR5", "haihai");
        Key key7 = new Key("BCNR5", "haihai");
        Key key8 = new Key("BCNR5", "haihai");
        List<Key> keys = new ArrayList<>();
        keys.add(key);
        keys.add(key1);
        keys.add(key2);
        keys.add(key3);
        keys.add(key4);
        keys.add(key5);
        keys.add(key6);
        keys.add(key7);
        keys.add(key8);

        Position position = new Position("abcd.pdf");
        MetaData metaData = new MetaData(new Index(keys));
        Record record = new Record(metaData, position);

        //17 889 203
        return record;
    }

}
