package InterfaceServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class InterfaceServer {
    public static void main(String[] args) throws FileNotFoundException {
        // Initialize variables
        int clientPort = 3000, dataServerPort = 3001;
        int loginKey;
        byte[] dataBuffer = new byte[65535];
        boolean loginValid = false, paymentValidFlag = false;

        String dataLogin;


        InetAddress dataServerAddress;
        PrintWriter dataServerOut;
        BufferedReader dataServerIn;

        DatagramPacket clientPacket, clientResponsePacket,
                dataServerPacket, dataServerResponsePacket;
        DatagramSocket clientSocket;
        Socket dataServerSocket;
        LinkedList<LoginNode> loginNodeLinkedList;
        LinkedList<CatalogNode> itemListLinkedList;

        HandleInterface userThread;

        // Initialize userLogins and itemList database from Content Root Path
        loginNodeLinkedList = initializeLoginDataFromFilePath("src/InterfaceServer/data/login.txt");
        itemListLinkedList = initializeCatalogFromFilePath("src/InterfaceServer/data/itemList.txt");

        // Set loginKey to default (4)
        loginKey = 4;

        try {
            // Set up TCP socket connection and scanners
            // TODO CHANGE TO DATA SERVER ADDRESS
            dataServerAddress = InetAddress.getByName("localhost");
            dataServerSocket = new Socket(dataServerAddress.getHostAddress(),dataServerPort);
            dataServerOut = new PrintWriter(dataServerSocket.getOutputStream(), true);
            dataServerIn = new BufferedReader(new InputStreamReader(dataServerSocket.getInputStream()));

            // Set up UDP socket connection with server port
            clientSocket = new DatagramSocket(clientPort);

            // Start thread loop
            while (true) {
                // Output ready message
                System.out.println("Ready for connection");

                // Create packet using dataBuffer and accept socket connection
                clientPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                clientSocket.receive(clientPacket);

                // Output busy message
                System.out.println("Connection busy");

                // Create and run login on user thread
                userThread = new HandleInterface(clientPacket, loginKey, clientSocket,
                        loginNodeLinkedList, itemListLinkedList);

                // Loop till login input is valid
                do {
                    // Check login input validation and set accountIndex
                    loginValid = userThread.login();

                    if (!loginValid) {
                        // Input invalid
                        // Reset buffer
                        dataBuffer = new byte[65535];

                        // Send back encrypted invalidation conformation to client
                        clientResponsePacket = new DatagramPacket(userThread.getLoginResult(false).getBytes(),
                                userThread.getLoginResult(false).length(), userThread.getClientAddress(),
                                userThread.getClientPort());
                        clientSocket.send(clientResponsePacket);

                        // Wait for new input
                        clientPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                        clientSocket.receive(clientPacket);

                        // Update client packet
                        userThread.updateClientPacket(clientPacket);
                    }

                } while (!loginValid);

                // TODO TESTING FROM HERE DOWN
                // Update login to DataServer
                dataLogin = userThread.getDataLogin();
                dataServerOut.println(dataLogin);

                // Send back conformation of input
                clientResponsePacket = new DatagramPacket(userThread.getLoginResult(true).getBytes(),
                        userThread.getLoginResult(true).length(), userThread.getClientAddress(),
                        userThread.getClientPort());
                clientSocket.send(clientResponsePacket);

                // Send list of items back to client
                clientResponsePacket = new DatagramPacket(userThread.getPreppedItemList().getBytes(),
                        userThread.getPreppedItemList().length(), userThread.getClientAddress(),
                        userThread.getClientPort());
                clientSocket.send(clientResponsePacket);

                // Wait for selection input
                clientPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                clientSocket.receive(clientPacket);

                // Update client packet selection
                userThread.updateClientPacket(clientPacket);
                userThread.updateSelection();

                // Send receipt back client
                clientResponsePacket = new DatagramPacket(userThread.getUserReceipt().getBytes(),
                        userThread.getUserReceipt().length(), userThread.getClientAddress(),
                        userThread.getClientPort());
                clientSocket.send(clientResponsePacket);

                // Loop till paymentInput is valid
                do {
                    // Wait for encryptedPayment input
                    clientPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                    clientSocket.receive(clientPacket);

                    // TODO TEST IF BROKE
                    // Forward encrypted creditData to DataServer
                    dataServerOut.println(new String(clientPacket.getData()));

                    if (dataServerIn.readLine().matches("1")) {
                        // Input valid
                        // Set flag
                        paymentValidFlag = true;

                        // Send conformation of valid payment to client
                        clientResponsePacket = new DatagramPacket("1".getBytes(), "1".length(),
                                userThread.getClientAddress(), userThread.getClientPort());
                        clientSocket.send(clientResponsePacket);

                    } else {
                        // Input invalid
                        // Send conformation of invalid payment to client
                        clientResponsePacket = new DatagramPacket("0".getBytes(), "0".length(),
                                userThread.getClientAddress(), userThread.getClientPort());
                        clientSocket.send(clientResponsePacket);
                    }

                } while (!paymentValidFlag);

                // Receive conformation of payment

                // Forward conformation of creditData to client

                // reset buffer for next request
                dataBuffer = new byte[65535];

            }
        } catch (Exception exception) {}



    }

    /**
     * Method: initializeLoginDataFromFilePath, Will create a file object using given file name and LinkedList object for
     * LoginNodes. Creates scanner that will input from file to create LoginNode objects. Will separate file input by
     * "-" characters and fill LoginNode objects with username, password, and key to populate LinkedList. When finished
     * will return populated LinkedList of LoginNodes.
     *
     * @param inputFilePath String, path of file
     * @return LinkedList Initialized database
     * @throws FileNotFoundException Suppress exception from file missing error
     */
    private static LinkedList<LoginNode> initializeLoginDataFromFilePath(String inputFilePath) throws FileNotFoundException {
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

    /**
     * Method: initializeFromFilePath, Will create a file object using given file name and LinkedList object for
     * CatalogNode. Creates scanner that will input from file to create CatalogNode objects. Will separate file input by
     * ":" characters and fill CatalogNode objects with indexNumber, name, and price to populate LinkedList. When finished
     * will return populated LinkedList of CatalogNode.
     *
     * @param inputFilePath String, path of file
     * @return LinkedList Initialized database
     * @throws FileNotFoundException Suppress exception from file missing error
     */
    private static LinkedList<CatalogNode> initializeCatalogFromFilePath(String inputFilePath) throws FileNotFoundException {
        // Create file and LinkedList
        File file = new File(inputFilePath);
        LinkedList<CatalogNode> linkedList = new LinkedList<>();

        // Create Scanner for data document
        Scanner fileScanner = new Scanner(file);

        // Input data from txt file to Linked list of dictionaryEntry objects
        while (fileScanner.hasNext()){
            // Input new line of test and split by tab chars
            String[] splitInput = fileScanner.nextLine().split(":");

            // Add LoginNode including username, password, and key to LinkedList
            linkedList.add(new CatalogNode(Integer.parseInt(splitInput[0].trim()),  // indexNumber
                    splitInput[1].trim(),                                           // name
                    Double.parseDouble(splitInput[2].trim())                        // price
            ));
        }

        // Returns data collection to main
        return linkedList;
    }

}
