import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class MyWatchService {

    private final String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).getParentFile().getPath();

    public MyWatchService() throws URISyntaxException {
    }

    public void watchDir() throws IOException, InterruptedException {
        System.out.println(jarDir);
        Path path = Paths.get(jarDir);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        int timeoutLen = 10;
        WatchKey key;
        boolean startFileFound = false;
        while ((key = watchService.poll(timeoutLen, TimeUnit.SECONDS)) != null) {
            String filename = "";
            for (WatchEvent<?> event : key.pollEvents()) {
                filename = event.context().toString();
                System.out.println("event kind: " + event.kind() + " file affected " + event.context());
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
            throw new RuntimeException("trigger file wasnt located within " + timeoutLen + "s");
        }


    }
}
