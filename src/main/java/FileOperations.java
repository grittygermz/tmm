import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperations {

    private final String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).getParentFile().getPath();

    public FileOperations() throws URISyntaxException {
    }

    public void removeFile(String filename) {
        try {
            System.out.println(jarDir);
            Path path = Paths.get(jarDir, filename);
            System.out.println(path.toAbsolutePath().toFile().getPath());

            Files.delete(path);
            System.out.println("deleted " + filename);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createFile(String filename) {
        try {
            Files.createFile(Paths.get(jarDir, filename));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
