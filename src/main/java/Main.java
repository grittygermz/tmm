import com.fasterxml.jackson.databind.ObjectMapper;
import compute.ComputeService;
import compute.InputValidator;
import compute.PDFService;
import compute.ZipService;
import compute.model.ArchiveFolder;
import compute.model.MaxFolderSizeUnit;
import compute.model.PDFMetadata;
import json.ArchivingObjectMapper;
import json.JobInput;
import lombok.extern.slf4j.Slf4j;
import sqlite.ArchiveRepository;
import sqlite.models.ArchivePack;
import sqlite.models.ArchiveSeq;
import util.StringTemplater;
import xml.XmlService;
import xml.audit.AuditFile;
import xml.audit.DataFile;
import xml.audit.Header;
import xml.audit.UnitOfWork;
import xml.meta.MetaFile;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class Main {
    public static void main(String[] args) throws URISyntaxException {

        try {
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
                log.error(e.getMessage(), e);
                terminate();
            }
            if(!InputValidator.validate(jobInput)) {
                throw new RuntimeException("input validation for jobinput failed either docId, typeOfMailing, title, jobId " +
                        "have exceeded the max allowable length");
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

            if (latestSeqNumToUse + archiveFolders.size() - 1 > jobInput.getSeqNumEnd()) {
                log.error("job requires [{}] seq nums to proceed. last seq num that will be produced is [{}] which will exceed " +
                                "the max provided of [{}], only [{}] seq nums are available",
                        archiveFolders.size(),
                        latestSeqNumToUse + archiveFolders.size() - 1,
                        jobInput.getSeqNumEnd(),
                        jobInput.getSeqNumEnd() - latestSeqNumToUse + 1);
                terminate();
            }


            //start of output creation
            PDFService pdfService = new PDFService();

            //XmlService xmlService = new XmlService();
            //StringTemplater stringTemplater = new StringTemplater(jobInput);
            StringTemplater stringTemplater = new StringTemplater();


            AtomicInteger atomicInteger = new AtomicInteger(latestSeqNumToUse);
            JobInput finalJobInput = jobInput;
            List<CompletableFuture<Void>> cflist = new ArrayList<>();
            archiveFolders.forEach(af -> {
                CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> {
                    try {
                        createOutputs(af, atomicInteger.getAndIncrement(), archiveRepository, archivePack, stringTemplater, finalJobInput, pdfService);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
                cflist.add(cf);
            });
            CompletableFuture.allOf(cflist.toArray(new CompletableFuture[cflist.size()])).join();
            //archiveFolders.stream().map(af -> {
            //            CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> {
            //                try {
            //                    createOutputs(af, atomicInteger.getAndIncrement(), archiveRepository, archivePack, xmlService, stringTemplater, finalJobInput, pdfService);
            //                } catch (IOException | InterruptedException e) {
            //                    throw new RuntimeException(e);
            //                }
            //                return null;
            //            });
            //            return cf;
            //        }).map(CompletableFuture::join)
            //        .collect(Collectors.toList());

            latestSeqNumToUse = atomicInteger.get();
            //remove the input folder created by designer
            log.info("removing input folder {}", jobInput.getArchiveFilesPath());
            removeDir(Paths.get(jobInput.getArchiveFilesPath()));

            persistEndOfJob(archivePack, latestSeqNumToUse, archiveRepository);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            terminate();
        }

        terminate();
    }

    private static void persistEndOfJob(ArchivePack archivePack, int latestSeqNumToUse, ArchiveRepository archiveRepository) {
        archivePack.setEndTime(new Date());
        archivePack.setSeqEnd(--latestSeqNumToUse);
        archiveRepository.updateArchivePackEntry(archivePack);
    }

    private static Map<String, String> createInitialParametersMap(JobInput jobInput) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("doctype", jobInput.getDocType());
        parameters.put("archiveid", jobInput.getArchiveId());
        parameters.put("bookingcenter", jobInput.getBookingCenter());
        parameters.put("date", jobInput.getDate());
        parameters.put("versionnum", String.valueOf(jobInput.getVersionNum()));
        return parameters;
    }

    private static void createOutputs(ArchiveFolder archiveFolder, int latestSeqNumToUse, ArchiveRepository archiveRepository, ArchivePack archivePack, StringTemplater stringTemplater, JobInput jobInput, PDFService pdfService) throws IOException, InterruptedException {
        XmlService xmlService = new XmlService();
        Map<String, String> params = createInitialParametersMap(jobInput);

        log.info("using seqNum {}", latestSeqNumToUse);
        //Thread.sleep(10000);

        int numDataFile = archiveFolder.getFileNameSizeMap().size();
        log.info("adding entry into archiveseq table for a folder");
        archiveRepository.createArchiveSeqEntry(
                new ArchiveSeq(latestSeqNumToUse,
                        numDataFile,
                        archiveFolder.getFolderSize(),
                        archivePack));

        //initialize default parameters for metafile
        //xmlService.initMetaFile();
        MetaFile metaFile = new MetaFile(new ArrayList<>());
        Map<String, Long> fileNameSizeMap = archiveFolder.getFileNameSizeMap();

        params.put("seqnum", String.format("%03d", latestSeqNumToUse));

        //initialize default parameters for auditfile
        Header header = new Header(jobInput.getDate(), getCurrentTime(), latestSeqNumToUse, jobInput.getVersionNum());
        AuditFile auditFile = new AuditFile(header, new UnitOfWork(jobInput.getArchiveId(), numDataFile + 1));
        //xmlService.initAuditFile(header, new UnitOfWork(jobInput.getArchiveId(), numDataFile + 1));

        // create temp folder to put generated files which will be then used to create the zip
        Path outputFolderPath = Paths.get(jobInput.getArchiveFilesPath()).getParent()
                .resolve(stringTemplater.getFolderName(params));
        try {
            Files.createDirectory(outputFolderPath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // iterate through map - key(pdf name), value(filesize)
        // will it get OOM here??
        // populates the meta and audit file objects in xmlservice
        int counter = 1;
        for (String initialFilename : fileNameSizeMap.keySet()) {

            Path initialFilePath = Paths.get(jobInput.getArchiveFilesPath()).resolve(initialFilename);
            PDFMetadata pdfMetadata = pdfService.extractPDFMetadata(initialFilePath.toFile());

            params.put("datanum", String.valueOf(counter));
            String datafilename = stringTemplater.getDatafileName(params);

            try {
                log.info("moving {} to {}", initialFilePath.toAbsolutePath(), outputFolderPath.resolve(datafilename).toAbsolutePath());
                Files.move(initialFilePath, outputFolderPath.resolve(datafilename), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            IndexKeys indexKeys = new IndexKeys(pdfMetadata.getLdd(), pdfMetadata.getBcnr(),
                    pdfMetadata.getKudennr(), jobInput.getGPN(), jobInput.getMESSAGE_ID(),
                    jobInput.getDocId(), jobInput.getTypeOfMailing(), jobInput.getTitle(),
                    jobInput.getJobId());
            xmlService.addRecordToMetaFile(indexKeys, datafilename, metaFile);

            Long filesize = fileNameSizeMap.get(initialFilename);
            xmlService.addUowDatafileContentsToAuditFile(new DataFile(datafilename, filesize), auditFile);

            counter++;
        }

        //creates the outputs after it is done creating the meta and audit objects in xmlservice
        //TODO create as function in separate class?
        String metafilename = stringTemplater.getMetafileName(params);
        log.info("creating metafile {}", outputFolderPath.resolve(metafilename).toAbsolutePath());
        //xmlService.createXmlFile(FileType.Meta, outputFolderPath.resolve(metafilename).toFile());
        xmlService.createMetaFile(metaFile, outputFolderPath.resolve(metafilename).toFile());

        //TODO create as function in separate class?
        BasicFileAttributes basicFileAttributes;
        try {
            basicFileAttributes = Files.readAttributes(outputFolderPath.resolve(metafilename), BasicFileAttributes.class);
            String auditfilename = stringTemplater.getAuditfileName(params);
            xmlService.addUowMetafileContentsToAuditFile(new DataFile(metafilename, basicFileAttributes.size()), auditFile);
            log.info("creating auditfile {}", outputFolderPath.resolve(auditfilename).toAbsolutePath());
            //xmlService.createXmlFile(FileType.Audit, outputFolderPath.resolve(auditfilename).toFile());
            xmlService.createAuditFile(auditFile, outputFolderPath.resolve(auditfilename).toFile());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        //TODO create as function in separate class?
        String controlfilename = stringTemplater.getControlfileName(params);
        try {
            log.info("creating controlfile {}", outputFolderPath.resolve(controlfilename).toAbsolutePath());
            Files.createFile(outputFolderPath.resolve(controlfilename));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        //do zipping
        Path zipFile = Paths.get(jobInput.getArchiveFilesPath()).getParent()
                .resolve(stringTemplater.getZipName(params));
        log.info("creating zip {}", zipFile.toAbsolutePath());
        new ZipService().zipParallel(outputFolderPath, zipFile);
        //new ZipService().zip4jZip(outputFolderPath, zipFile.toFile());

        //remove the temp folder which generated meta,audit,control files were placed
        log.info("removing temp folder {}", outputFolderPath.toAbsolutePath());
        removeDir(outputFolderPath);
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
        try (Stream<Path> walk = Files.walk(dirPath)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(f -> {
                        try {
                            Files.delete(f);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            terminate();
                        }
                    });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            terminate();
        }
    }


}

