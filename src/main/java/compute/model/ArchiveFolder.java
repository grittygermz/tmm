package compute.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ArchiveFolder {
    private Map<String, Long> fileNameSizeMap;
    private long folderSize;

    public ArchiveFolder() {
        this.fileNameSizeMap = new HashMap<>();
    }
}
