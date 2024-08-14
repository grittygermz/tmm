import java.io.File;
import java.io.IOException;

public class FileOperations {
    public void removeFile(String filename) {
        File file = new File(filename);
        if (file.delete()) {
            System.out.println("Deleted the file: " + file.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }

    public void createFile(String filename) {
        File file = new File(filename);
        try {
            boolean newFile = file.createNewFile();
            if(newFile) {
                System.out.println("file created " + filename);
            } else {
                System.out.println("failed to create " + filename);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
