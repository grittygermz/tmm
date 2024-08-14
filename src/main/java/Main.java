import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("started");

        FileOperations fileOperations = new FileOperations();
        fileOperations.removeFile("jobDetails.json");
        fileOperations.removeFile("jobSeq.json");

        Scanner scanner = new Scanner(System.in);
        System.out.println("waiting for input");
        String s = scanner.nextLine();
        fileOperations.removeFile("jobSeq.json");
        System.out.println(s);
    }


}
