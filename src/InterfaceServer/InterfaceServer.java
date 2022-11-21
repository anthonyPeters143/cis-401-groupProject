package InterfaceServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.Scanner;

public class InterfaceServer {
    public static void main(String[] args) throws FileNotFoundException {
        // Initialize variables
        int serverPort = 3000;
        int loginKey = 4;
        byte[] dataBuffer = new byte[65535];
        String loginFileName = "login.txt";
        DatagramPacket clientPacket, DataServerPacket;
        DatagramSocket interfaceSocket;
        LinkedList<LoginNode> loginNodeLinkedList;

        // Initialize database from Content Root
        loginNodeLinkedList = initializeFromFilePath("src/InterfaceServer/data/login.txt");

        try {
            // Set up connection with client
            interfaceSocket = new DatagramSocket(serverPort);

            // Start thread loop
            while (true) {
                // Output ready message
                System.out.println("Ready for connection");

                // Create packet using dataBuffer and accept socket connection
                clientPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                interfaceSocket.receive(clientPacket);

                // Output busy message
                System.out.println("Connection busy");

                // Create and run new thread
                new

                // reset buffer for next request


            }

        } catch (Exception exception) {}



    }

    /**
     * Method: initializeFromFilePath, Will create a file object using given file name and LinkedList object for
     * LoginNodes. Creates scanner that will input from file to create LoginNode objects. Will separate file input by
     * "\t" characters and fill LoginNode objects with username, password, key to populate LinkedList. When finished
     * will return populated LinkedList of LoginNodes.
     *
     * @param inputFilePath String, path of file
     * @return LinkedList Initialized database
     * @throws FileNotFoundException Suppress exception from file missing error
     */
    private static LinkedList<LoginNode> initializeFromFilePath(String inputFilePath) throws FileNotFoundException {
        // Create file and LinkedList
        File file = new File(inputFilePath);
        LinkedList<LoginNode> linkedList = new LinkedList<>();

        // Create Scanner for data document
        Scanner fileScanner = new Scanner(file);

        // Input data from txt file to Linked list of dictionaryEntry objects
        while (fileScanner.hasNext()){
            // Input new line of test and split by tab chars
            String[] splitInput = fileScanner.nextLine().split("\t");

            // Add LoginNode including username, password, and key to LinkedList
            linkedList.add(new LoginNode(splitInput[0].trim(),  // username
                    splitInput[1].trim(),                       // password
                    Integer.parseInt(splitInput[2].trim()));    // key
        }

        // Returns data collection to main
        return linkedList;
    }
}
