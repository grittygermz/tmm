import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        System.out.println("started");

        MyWatchService myWatchService = new MyWatchService();
        myWatchService.watchDir();

        //FileOperations fileOperations;
        //try {
        //    fileOperations = new FileOperations();
        //} catch (URISyntaxException e) {
        //    System.out.println(e.getMessage());
        //    throw new RuntimeException(e);
        //}
        //fileOperations.removeFile("jobDetails.json");
        //fileOperations.removeFile("jobSeq.json");

        Scanner scanner = new Scanner(System.in);
        System.out.println("waiting for input to terminate");
        String s = scanner.nextLine();
        //fileOperations.removeFile("jobSeq.json");
        //fileOperations.createFile("jobSeqUpd.json");
        //System.out.println(s);
    }


}
