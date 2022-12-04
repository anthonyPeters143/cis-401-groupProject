package DataServer;

import InterfaceServer.LoginNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class DataServer {
    // TODO TESTING FROM HERE DOWN

    public static void main(String[] args) throws FileNotFoundException {
        // Initialize variables
        int dataServerPort;

        ServerSocket dataServerServerSocket;
        Socket dataServerSocket;
        InetAddress interfaceServerAddress;
        LinkedList<PaymentNode> paymentNodeLinkedList;

        HandleData serverThread;

        // Initialize database from Content Root Path
        paymentNodeLinkedList = initializePaymentFromFilePath("src/DataServer/data/creditInfo.txt");

        try {
            // Set up connection information
            dataServerPort = 3001;

            // Create server on dataServerPort
            dataServerServerSocket = new ServerSocket(dataServerPort);

            while (true) {
                // Output ready message
                System.out.println("Ready for connection");

                dataServerSocket = dataServerServerSocket.accept();

                // Output busy message
                System.out.println("Connection busy");

                // Create HandleData thread with socket connection info and payment database
                serverThread = new HandleData(dataServerSocket, paymentNodeLinkedList);
            }

        } catch (Exception exception) {}
    }

    /**
     * Method: initializePaymentFromFilePath, Will create a file object using given file name and LinkedList object for
     * PaymentNodes. Creates scanner that will input from file to create PaymentNode objects. Will separate file input by
     * "-" characters and fill PaymentNode objects with username, password, key, creditCard and securityCode to populate
     * LinkedList. When finished will return populated LinkedList of PaymentNodes.
     *
     * @param inputFilePath String, path of file
     * @return LinkedList Initialized database
     * @throws FileNotFoundException Suppress exception from file missing error
     */
    private static LinkedList<PaymentNode> initializePaymentFromFilePath(String inputFilePath) throws FileNotFoundException {
        // Create file and LinkedList
        File file = new File(inputFilePath);
        LinkedList<PaymentNode> linkedList = new LinkedList<>();

        // Create Scanner for data document
        Scanner fileScanner = new Scanner(file);

        // Input data from txt file to Linked list of dictionaryEntry objects
        while (fileScanner.hasNext()){
            // Input new line of test and split by tab chars
            String[] splitInput = fileScanner.nextLine().split("-");

            // Add LoginNode including username, password, and key to LinkedList
            linkedList.add(new PaymentNode(
                    splitInput[0].trim(),                   // username
                    splitInput[1].trim(),                   // password
                    Integer.parseInt(splitInput[2].trim()), // key
                    splitInput[3],                          // creditCard
                    splitInput[4]));                        // securityCode
            }

        // Returns data collection to main
        return linkedList;
    }
}
