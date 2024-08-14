import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperations {
    public void removeFile(String filename) {
        try {
            Path path = Paths.get(filename);
            System.out.println(path.toAbsolutePath().toFile().getPath());

            String path1 = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getPath();
            System.out.println(path1);

            Files.delete(path);
            System.out.println("deleted " + filename);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
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
