import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class MyWatchService {

    private final String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).getParentFile().getPath();

    public MyWatchService() throws URISyntaxException {
    }

    public void watchDir(WatchService watchService) throws IOException, InterruptedException {

        //System.out.println(jarDir);
        //Path path = Paths.get(jarDir);
        //WatchService watchService = FileSystems.getDefault().newWatchService();
        //path.register(watchService, ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

        int timeoutLen = 30;
        WatchKey key;
        boolean startFileFound = false;
        while ((key = watchService.poll(timeoutLen, TimeUnit.SECONDS)) != null) {
            String filename = "";
            for (WatchEvent<?> event : key.pollEvents()) {
                filename = event.context().toString();
                System.out.println(filename);
                Path p = (Path) event.context();
                String absPath = p.toAbsolutePath().toFile().getAbsolutePath();

                System.out.println("event kind: " + event.kind() + " file affected " + absPath);
            }
            if(filename.equalsIgnoreCase("jobdetails.json")) {
                startFileFound = true;
                break;
            }
            key.reset();
        }
        System.out.println("stopped watching");
        System.out.println("target file was found = " + startFileFound);
        if(!startFileFound) {
            log.error("trigger file wasnt located within {}s", timeoutLen);
        }

    }

    public WatchService watchRecursiveSetup() throws IOException {


        WatchService watchService = FileSystems.getDefault().newWatchService();

        Files.walkFileTree(Paths.get(jarDir), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                FileTime fileTime = attrs.lastModifiedTime();
                System.out.println(dir.toAbsolutePath().toFile().getAbsolutePath() + " " + fileTime);
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return super.preVisitDirectory(dir, attrs);
            }
        });

        return watchService;
    }
}
