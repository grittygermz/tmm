package compute;

import compute.model.ArchiveFolder;
import compute.model.MaxFolderSizeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ComputeService {

    private final long maxFolderSize;
    private final int maxNumOfFilesInFolder;
    private final Map<String, Long> fileNameAndSizeMap = new HashMap<>();

    public ComputeService(int maxFolderSize, MaxFolderSizeUnit maxFolderSizeUnit, int maxNumOfFilesInFolder) {
        this.maxNumOfFilesInFolder = maxNumOfFilesInFolder;

        //convert it from whatever units to bytes for comparison later
        switch (maxFolderSizeUnit) {
            case GB:
                log.info("units of GB was provided");
                this.maxFolderSize = maxFolderSize * 1024L * 1024L * 1024L;
                break;
            case MB:
                log.info("units of MB was provided");
                this.maxFolderSize = maxFolderSize * 1024L * 1024L;
                break;
            case KB:
                log.info("units of KB was provided");
                this.maxFolderSize = maxFolderSize * 1024L;
                break;
            default:
                throw new RuntimeException("invalid file unit");
        }
    }

    public void createInputPDFMap(Path pdfSrcDir) {
        try {
            Files.list(pdfSrcDir).forEach(file -> {
                try {
                    BasicFileAttributes basicFileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                    fileNameAndSizeMap.put(file.toString(), basicFileAttributes.size());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.debug("contents of fileNameAndSizeMap {}", fileNameAndSizeMap);
    }

    public List<ArchiveFolder> organizeFilesIntoFolders() {
        List<ArchiveFolder> zipFolders = new ArrayList<>();
        zipFolders.add(new ArchiveFolder());

        // sort such that largest is first and smallest is last
        LinkedHashMap<String, Long> collect = fileNameAndSizeMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        //System.out.println(collect);

        for (Map.Entry<String, Long> entry : collect.entrySet()) {
            boolean wasAddedToFolder = false;
            for (ArchiveFolder folder : zipFolders) {
                if (folder.getFileNameSizeMap().size() >= maxNumOfFilesInFolder) {
                    continue;
                }
                long newFolderSize = folder.getFolderSize() + entry.getValue();
                if (newFolderSize > maxFolderSize) {
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
            log.debug("contents zipFolders {}", folder);
        }
        return zipFolders;
    }
}
