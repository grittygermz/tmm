import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperations {
    public void removeFile(String filename) {
        try {
            Path path = Paths.get(filename);
            System.out.println(path.toFile().getPath());
            Files.delete(path);
            System.out.println("deleted " + filename);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createFile(String filename) {
        try {
            Files.createFile(Paths.get(filename));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
