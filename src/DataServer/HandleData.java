package DataServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Class: HandleData, Class created by DataServer to connect to InterfaceServer using a TCP connection. System will loop
 * searching for user PaymentNode using account details, then decode data from InterfaceServer to check if payment input
 * is valid and respond with result to InterfaceServer.
 *
 * @author Anthony Peters
 */
public class HandleData {
    PrintWriter dataServerOut;
    BufferedReader dataServerIn;
    PaymentNode userPaymentNode;
    LinkedList<PaymentNode> paymentNodeLinkedList;
    Socket dataServerSocket;


    /**
     * Method: HandleData constructor, Augment-constructor that needs DataServer Socket and paymentNode Linked List.
     * When starting will set up scanners for TCP connection to DataServer then wait for input from DataServer to
     * search for user paymentNode within LinkedList. System will loop decoding using user key value, checking inputs of
     * credit card and security codes. If valid then respond to InterfaceServer with a 1 character to InterfaceServer,
     * if invalid then respond to InterfaceServer with a 0 character to InterfaceServer.
     *
     * @param dataServerSocketInput Socket, DataServerSocket
     * @param paymentNodeLinkedListInput LinkedList<PaymentNode> PaymentNodeLinkedList, Linked List of PaymentNodes
     */
    HandleData(Socket dataServerSocketInput, LinkedList<PaymentNode> paymentNodeLinkedListInput) {
        // Initialize parameters
        boolean paymentValidFlag = false;
        dataServerSocket = dataServerSocketInput;
        paymentNodeLinkedList = paymentNodeLinkedListInput;

        try {
            // Set up interfaceServer scanners
            dataServerOut = new PrintWriter(dataServerSocket.getOutputStream(), true);
            dataServerIn = new BufferedReader(new InputStreamReader(dataServerSocket.getInputStream()));

            // Receive valid login info from interfaceServer, find matching PaymentNode
            userPaymentNode = findUserPaymentNode(dataServerIn.readLine());

            // Loop till payment is valid
            do {
                // Decode payment info using userKey then validate info
                if (validatePayment(keyPaymentDecoding(dataServerIn.readLine().trim(), userPaymentNode.getKey()))) {
                    // Set paymentValidFlag
                    paymentValidFlag = true;

                    // Send to interfaceServer payment valid signal
                    dataServerOut.println("1");
                } else {
                    // Send to interfaceServer payment invalid signal
                    dataServerOut.println("0");
                }
            } while (!paymentValidFlag);
        } catch (Exception exception) {};
    }

    /**
     * Method: validatePayment, Method will take String value in the pattern of "creditCard-securityCode" and split by
     * '-' character, then check values against stored user account details and return result of match.
     *
     * @param paymentDetailInput String, String value of "creditCard-securityCode"
     * @return boolean, Results if account details match input
     */
    private boolean validatePayment(String paymentDetailInput) {
        // Initialize variables
        String[] paymentDetail = paymentDetailInput.split("-");

        // Check if credit card and security code match userPaymentNode parameters
        return Objects.equals(userPaymentNode.getCreditCard(), paymentDetail[0]) &&
                Objects.equals(userPaymentNode.getSecurityCode(), paymentDetail[1]);
    }

    /**
     * Method: findUserPaymentNode, Method will take String input in pattern of "username:password" and split by :
     * character then check it against all PaymentNodes in LinkedList.
     *
     * @param paymentID String, String value of "username:password"
     * @return PaymentNode, User paymentNode from LinkedList
     */
    private PaymentNode findUserPaymentNode(String paymentID) {
        // Initialize variables
        String[] loginInput = paymentID.split(":");
        PaymentNode userPaymentNode;

        // Loop through paymentNodeLinkedList for node with matching username and password parameters
        for (int index = 0; index < paymentNodeLinkedList.size(); index++) {
            // Set paymentNode from index
            userPaymentNode = paymentNodeLinkedList.get(index);

            // Check if username and password parameters match input
            if (userPaymentNode.getUsername().matches(loginInput[0]) &&
            userPaymentNode.getPassword().matches(loginInput[1])) {
                // Return userPaymentNode
                return userPaymentNode;
            }
        }

        // If not found return null value
        return null;
    }

    /**
     * Method: keyEncoding, Method receives a message to encrypt and a key to encrypt it with. Message will be
     * encrypted using a ceaser cipher shifted by key's amount. Method converts message string into a character array,
     * then cycles though array using a for loop. For each character their value is shifted by the key's amount then the
     * encrypted version is added to end of encrypted string variable which is passed back to original method.
     *
     * @param message String, Message to be encrypted
     * @param key int, Key value to be used in ceaser cipher encryption
     * @return String, Encrypted message
     */
    private static String keyEncoding(String message, int key) {
        final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char messageChar;
        char[] messageCharArray = message.toCharArray();
        String encodedMessage = "";

        // Loop though message chars
        for (int i = 0; i < message.length(); i++) {
            // Initialize char from array
            messageChar = messageCharArray[i];

            // Test char and encode if possible
            if (messageChar == '_' || messageChar == '-')
            {
                // Skip encoding the dividing char
                encodedMessage = encodedMessage.concat(String.valueOf(messageChar));

            } else if (String.valueOf(messageChar).matches("\\d") ) {
                // Char is numerical
                encodedMessage = encodedMessage.concat(String.valueOf((char) (key + (int) messageChar)));

            } else {
                // Find position of char within alphabet then concat the encoded char to message
                int position = (key + ALPHABET.indexOf(messageChar)) % 26;

                encodedMessage = encodedMessage.concat(String.valueOf(
                        ALPHABET.charAt(position))
                );

            }
        }

        return encodedMessage;
    }

    /**
     * Method: keyPaymentDecoding, Method receives a message to decrypt and a key to decrypt it with. Message will be
     * decrypted using a ceaser cipher shifted backwards by key's amount. Method converts message string into a
     * character array, then cycles though array using a for loop. For each character their value is shifted by the
     * key's amount backwards then the decrypted version is added to end of decrypted string variable which is passed
     * back to original method.
     *
     * @param message String, Message to be decrypted
     * @param key int, Key value to be used in ceaser cipher encryption
     * @return String, Decrypted message
     */
    private static String keyPaymentDecoding(String message, int key) {
        final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char messageChar;
        char[] messageCharArray = message.toCharArray();
        String decodedMessage = "";

        // Loop though message chars
        for (int i = 0; i < message.length(); i++) {
            // Initialize char from array
            messageChar = messageCharArray[i];

            // Test char and decode if possible
            if (messageChar == '_' || messageChar == '-')
            {
                // Skip decoding the dividing char
                decodedMessage = decodedMessage.concat(String.valueOf(messageChar));
            } else {
                // Char is numerical
                decodedMessage = decodedMessage.concat(String.valueOf((char) ((int) messageChar - key)));
            }
        }

        return decodedMessage;
    }

}
