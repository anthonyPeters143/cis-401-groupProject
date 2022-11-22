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
        int loginKey;
        byte[] dataBuffer = new byte[65535];
        boolean loginValid = false;
//        String loginFileName = "login.txt";
        DatagramPacket clientPacket, DataServerPacket;
        DatagramSocket interfaceSocket;
        LinkedList<LoginNode> loginNodeLinkedList;

        HandleInterface userThread;

        // Initialize database from Content Root
        loginNodeLinkedList = initializeFromFilePath("src/InterfaceServer/data/login.txt");

        // Set loginKey to default (4)
        loginKey = 4;

        try {
            // Set up socket connection with server port
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

                // Loop till login input is valid
                do {
                    // Create and run login on user thread
                    userThread = new HandleInterface(clientPacket, loginKey, interfaceSocket, loginNodeLinkedList);

                    // Check login input validation and set accountIndex
                    loginValid = userThread.login();

                    if (!loginValid) {
                        // Input invalid
                        // Reset buffer

                        // Send back invalidation conformation to client

                        // Wait for new input
                    }

                } while (!loginValid);

                // Send back conformation of input

                // Send list of items back to client

                // Wait for selection

                // Create receipt

                // Wait for encrypted creditData

                // Forward encrypted creditData to DataServer

                // Forward conformation of creditData to client

                // reset buffer for next request

            }
        } catch (Exception exception) {}



    }

    /**
     * Method: initializeFromFilePath, Will create a file object using given file name and LinkedList object for
     * LoginNodes. Creates scanner that will input from file to create LoginNode objects. Will separate file input by
     * "-" characters and fill LoginNode objects with username, password, key to populate LinkedList. When finished
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
            String[] splitInput = fileScanner.nextLine().split("-");

            // Add LoginNode including username, password, and key to LinkedList
            linkedList.add(new LoginNode(splitInput[0].trim(),  // username
                    splitInput[1].trim(),                       // password
                    Integer.parseInt(splitInput[2].trim())));    // key
        }

        // Returns data collection to main
        return linkedList;
    }
}
