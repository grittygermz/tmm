import com.fasterxml.jackson.databind.ObjectMapper;
import compute.ComputeService;
import compute.PDFService;
import compute.model.ArchiveFolder;
import compute.model.MaxFolderSizeUnit;
import compute.model.PDFMetadata;
import json.ArchivingObjectMapper;
import json.JobInput;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import sqlite.ArchiveRepository;
import sqlite.models.ArchivePack;
import sqlite.models.ArchiveSeq;
import util.StringTemplater;
import xml.FileType;
import xml.XmlService;
import xml.audit.DataFile;
import xml.audit.Header;
import xml.audit.UnitOfWork;
import xml.metaDTO.IndexKeys;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class Main {
    public static void main(String[] args) throws URISyntaxException {
        String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParentFile().getPath();

        log.info("looking for jobinput.json in {}", jarDir);

        // read the input file
        //TODO change the path to jarDir
        //jarDir = "src/test/resources";
        ObjectMapper objectMapper = ArchivingObjectMapper.getInstance();
        JobInput jobInput = null;
        try {
            jobInput = objectMapper.readValue(new File(jarDir, "jobInput.json"), JobInput.class);
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
            terminate();
        }
        log.info("jobinput received {}", jobInput);

        // check against the db
        ArchiveRepository archiveRepository = new ArchiveRepository();
        archiveRepository.setupDatabase();


        String currentPackPrefix = jobInput.getDocType() + jobInput.getArchiveId() + jobInput.getBookingCenter() + jobInput.getDate() + jobInput.getVersionNum();

        int latestSeqNumToUse = archiveRepository.getLatestSeqNumToUse(currentPackPrefix);
        log.info("attempting to use seqNum [{}] ", latestSeqNumToUse);
        if (latestSeqNumToUse == 0) {
            latestSeqNumToUse = jobInput.getSeqNumStart();
        }
        if (latestSeqNumToUse > jobInput.getSeqNumEnd()) {
            log.error("next seq number [{}] to use exceeds the max allowable of [{}] from jobinput",
                    latestSeqNumToUse, jobInput.getSeqNumEnd());
            terminate();
        }


        //populate archivepack table to capture start
        ArchivePack archivePack = new ArchivePack(currentPackPrefix, latestSeqNumToUse, new Date());
        int archivePackId = archiveRepository.createArchivePackEntry(archivePack);
        //System.out.println(archivePackId);

        //initialize the parameters for computing
        ComputeService computeService;
        if (jobInput.getMaxFolderSizeKB() > 0) {
            computeService = new ComputeService(jobInput.getMaxFolderSizeKB(), MaxFolderSizeUnit.KB, jobInput.getMaxDataFiles());
        } else if (jobInput.getMaxFolderSizeMB() > 0) {
            computeService = new ComputeService(jobInput.getMaxFolderSizeMB(), MaxFolderSizeUnit.MB, jobInput.getMaxDataFiles());
        } else {
            computeService = new ComputeService(jobInput.getMaxFolderSizeGB(), MaxFolderSizeUnit.GB, jobInput.getMaxDataFiles());
        }

        //iterate through pdf dir and get filenames and sizes into a map
        computeService.createInputPDFMap(Paths.get(jobInput.getArchiveFilesPath()));

        //organize pdfs in their respective folders
        log.info("starting assigning files to their respective folders");
        List<ArchiveFolder> archiveFolders = computeService.organizeFilesIntoFolders();
        log.info("completed assigning files to their respective folders");
        log.info("num of sequence numbers to be used = {}", archiveFolders.size());

        //TODO check if remaining sequence numbers is sufficient to be used
        if(latestSeqNumToUse + archiveFolders.size() - 1 > jobInput.getSeqNumEnd()) {
            log.error("job requires [{}] seq nums to proceed. last seq num that will be produced is [{}] which will exceed " +
                    "the max provided of [{}], only [{}] seq nums are available",
                    archiveFolders.size(),
                    latestSeqNumToUse + archiveFolders.size() - 1,
                    jobInput.getSeqNumEnd(),
                    jobInput.getSeqNumEnd() - latestSeqNumToUse + 1);
            terminate();
        }


        //start of output creation
        XmlService xmlService = new XmlService();
        PDFService pdfService = new PDFService();

        StringTemplater stringTemplater = new StringTemplater(jobInput);

        for (ArchiveFolder archiveFolder : archiveFolders) {
            log.info("using seqNum {}", latestSeqNumToUse);

            int numDataFile = archiveFolder.getFileNameSizeMap().size();
            log.info("adding entry into archiveseq table for a folder");
            archiveRepository.createArchiveSeqEntry(
                    new ArchiveSeq(latestSeqNumToUse,
                            numDataFile,
                            archiveFolder.getFolderSize(),
                            archivePack));
            xmlService.initMetaFile();
            Map<String, Long> fileNameSizeMap = archiveFolder.getFileNameSizeMap();

            stringTemplater.getParametersMap().put("seqnum", String.format("%03d", latestSeqNumToUse));

            Header header = new Header(jobInput.getDate(), getCurrentTime(), latestSeqNumToUse, jobInput.getVersionNum());
            xmlService.initAuditFile(header, new UnitOfWork(jobInput.getArchiveId(), numDataFile + 1));

            Path outputFolderPath = Paths.get(jobInput.getArchiveFilesPath()).getParent()
                    .resolve(stringTemplater.getFolderName());
            try {
                Files.createDirectory(outputFolderPath);
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
                terminate();
            }

            int counter = 1;
            for (String initialFilename : fileNameSizeMap.keySet()) {

                Path initialFilePath = Paths.get(jobInput.getArchiveFilesPath()).resolve(initialFilename);
                PDFMetadata pdfMetadata = pdfService.extractPDFMetadata(initialFilePath.toFile());

                stringTemplater.getParametersMap().put("datanum", String.valueOf(counter));
                String datafilename = stringTemplater.getDatafileName();

                try {
                    log.info("moving {} to {}", initialFilePath.toAbsolutePath(), outputFolderPath.resolve(datafilename).toAbsolutePath());
                    Files.move(initialFilePath, outputFolderPath.resolve(datafilename), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error(Arrays.toString(e.getStackTrace()));
                    terminate();
                }

                IndexKeys indexKeys = new IndexKeys(pdfMetadata.getLdd(), pdfMetadata.getBcnr(),
                        pdfMetadata.getKudennr(), jobInput.getGPN(), jobInput.getMESSAGE_ID());
                xmlService.addRecordToMetaFile(indexKeys, datafilename);

                Long filesize = fileNameSizeMap.get(initialFilename);
                xmlService.addUowDatafileContentsToAuditFile(new DataFile(datafilename, filesize));

                counter++;
            }

            //TODO create as function in xmlservice?
            String metafilename = stringTemplater.getMetafileName();
            log.info("creating metafile {}", outputFolderPath.resolve(metafilename).toAbsolutePath());
            xmlService.createXmlFile(FileType.Meta, outputFolderPath.resolve(metafilename).toFile());

            //TODO create as function in xmlservice?
            BasicFileAttributes basicFileAttributes;
            try {
                basicFileAttributes = Files.readAttributes(outputFolderPath.resolve(metafilename), BasicFileAttributes.class);
                String auditfilename = stringTemplater.getAuditfileName();
                xmlService.addUowMetafileContentsToAuditFile(new DataFile(metafilename, basicFileAttributes.size()));
                log.info("creating auditfile {}", outputFolderPath.resolve(auditfilename).toAbsolutePath());
                xmlService.createXmlFile(FileType.Audit, outputFolderPath.resolve(auditfilename).toFile());
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
                terminate();
            }

            //TODO create as function in xmlservice?
            String controlfilename = stringTemplater.getControlfileName();
            try {
                log.info("creating controlfile {}", outputFolderPath.resolve(controlfilename).toAbsolutePath());
                Files.createFile(outputFolderPath.resolve(controlfilename));
            } catch (IOException e) {
                log.error(Arrays.toString(e.getStackTrace()));
                terminate();
            }

            //TODO create as function in main?
            //do zipping
            File zipFile = Paths.get(jobInput.getArchiveFilesPath()).getParent()
                    .resolve(stringTemplater.getZipName()).toFile();
            log.info("creating zip {}", zipFile.getAbsolutePath());

            try {
                Files.list(outputFolderPath).forEach(pathz -> {
                    try {
                        new ZipFile(zipFile).addFile(pathz.toFile());
                    } catch (ZipException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //remove the temp folder which generated meta,audit,control files were placed
            log.info("removing temp folder {}", outputFolderPath.toAbsolutePath());
            removeDir(outputFolderPath);

            latestSeqNumToUse++;
        }

        //remove the input folder created by designer
        log.info("removing input folder {}", jobInput.getArchiveFilesPath());
        removeDir(Paths.get(jobInput.getArchiveFilesPath()));

        //TODO create as function in main?
        archivePack.setEndTime(new Date());
        archivePack.setSeqEnd(--latestSeqNumToUse);
        archiveRepository.updateArchivePackEntry(archivePack);

        terminate();
    }

    public static String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HHmmss");
        return LocalTime.now(ZoneOffset.UTC).format(dtf);
    }

    public static void terminate() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("waiting for input to terminate");
        scanner.nextLine();
        System.exit(0);
    }

    public static void removeDir(Path dirPath) {
        try(Stream<Path> walk = Files.walk(dirPath)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(f -> {
                        try {
                            Files.delete(f);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

