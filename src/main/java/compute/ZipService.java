package compute;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

@Slf4j
public class ZipService {

    public void zip4jZip(Path outputFolderPath, File zipFile) {
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
    }

    public void zipParallel(Path sourceFolderPath, Path zipFilePath) throws IOException {

        try (OutputStream outputStream = Files.newOutputStream(zipFilePath);
             ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {

            ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();

            zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);

            int srcFolderLength = sourceFolderPath
                    .toAbsolutePath().toString().length() + 1;  // +1 to remove the last file separator

            try (Stream<Path> files = Files.list(sourceFolderPath)) {
                files.forEach(file -> {
                    String relativePath = file.toAbsolutePath().toString()
                            .substring(srcFolderLength);

                    InputStreamSupplier streamSupplier = () -> {
                        InputStream is = null;
                        try {
                            is = Files.newInputStream(file);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                        return is;
                    };
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(relativePath);
                    zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                    scatterZipCreator.addArchiveEntry(zipArchiveEntry, streamSupplier);
                });
            }
            scatterZipCreator.writeTo(zipArchiveOutputStream);
        } catch (ExecutionException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
