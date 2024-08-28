import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class SampleWatchDir {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final int timeoutLen = 30;
    private final long folderAgeBoundary = 5 * 60;
    //private final boolean recursive;
    //private boolean trace = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        //if (trace) {
            Path prev = keys.get(key);
            //if (prev == null) {
                System.out.format("register: %s\n", dir);
            //} else {
            //    if (!dir.equals(prev)) {
            //        System.out.format("update: %s -> %s\n", prev, dir);
            //    }
            //}
        //}
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
            {
                FileTime fileTime = attrs.lastModifiedTime();
                System.out.println(dir.toAbsolutePath() + ": " + fileTime);
                long FIVE_MIN = 5 * 60;
                long sec = Instant.now().getEpochSecond() - fileTime.toInstant().getEpochSecond();
                System.out.println("time diff " + sec);
                if (sec <= folderAgeBoundary){
                    System.out.println("is a recent folder");
                    register(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    SampleWatchDir() throws IOException, URISyntaxException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        //this.recursive = recursive;

        Path jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParentFile().toPath();
        registerAll(jarDir);
        //if (recursive) {
        //    System.out.format("Scanning %s ...\n", dir);
        //    registerAll(dir);
        //    System.out.println("Done.");
        //} else {
        //    register(dir);
        //}

        // enable trace after initial registration
        //this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() throws InterruptedException {


        // wait for key to be signalled
        WatchKey key;
        boolean startFileFound = false;
        while ((key = watcher.poll(timeoutLen, TimeUnit.SECONDS)) != null) {

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            String filename = "";
            WatchEvent.Kind kind = null;

            for (WatchEvent<?> event : key.pollEvents()) {
                kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);
                filename = name.toString();

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                // assume that directory is created before prog is triggered to simplify
                //if (recursive && (kind == ENTRY_CREATE)) {
                //    try {
                //        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                //            registerAll(child);
                //        }
                //    } catch (IOException x) {
                //        // ignore to keep sample readbale
                //    }
                //}
            }

            log.info("@@" + filename);

            if(filename.toString().equalsIgnoreCase("tmm.done") && kind.equals(ENTRY_CREATE)) {
                log.info("found");
                startFileFound = true;
                break;
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }

        System.out.println("stopped watching");
        System.out.println("target file was found = " + startFileFound);
        if(!startFileFound) {
            log.error("trigger file wasnt located within {}s", timeoutLen);
        }

    }


    //public static void main(String[] args) throws IOException, InterruptedException {
    //
    //
    //    // register directory and process its events
    //    //Path dir = Paths.get("C:\\Users\\Alex\\IdeaProjects\\tmmSimple\\tmmSimple\\target");
    //    new SampleWatchDir().processEvents();
    //}
}
