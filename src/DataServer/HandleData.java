package DataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

public class HandleData {
    PrintWriter dataServerOut;
    BufferedReader dataServerIn;

    PaymentNode userPaymentNode;

    DatagramPacket interfacePacket;
    LinkedList<PaymentNode> paymentNodeLinkedList;

    Socket dataServerSocket;


    HandleData(Socket dataServerSocketInput, LinkedList<PaymentNode> paymentNodeLinkedListInput) {
        // Initialize parameters
        boolean paymentValidFlag = false;

        String[] loginInput;
        String usernameInput, passwordInput;
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
                // TODO ERROR STARTING HERE
                if (validatePayment(keyDecoding(dataServerIn.readLine().trim(), userPaymentNode.getKey()))) {
                    // Set paymentValidFlag
                    paymentValidFlag = true;

                    // Send to interfaceServer payment valid signal
                    dataServerOut.println("1");

                } else {
                    // Send to interfaceServer payment invalid signal
                    dataServerOut.println("0");

                }

            } while (!paymentValidFlag);

            // Validate payment info after decoding using userKey


        } catch (Exception exception) {};
    }

    private boolean validatePayment(String paymentDetailInput) {
        // Initialize variables
        String[] paymentDetail = paymentDetailInput.split("-");

        // Check if credit card and security code match userPaymentNode parameters
        return Objects.equals(userPaymentNode.getCreditCard(), paymentDetail[0]) &&
                Objects.equals(userPaymentNode.getSecurityCode(), paymentDetail[1]);
    }

    // TODO UPDATE FOR REDUNDANCY
    private PaymentNode findUserPaymentNode(String paymentID) throws IOException {
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
                return userPaymentNode;
            }
        }

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
     * Method: keyDecoding, Method receives a message to decrypt and a key to decrypt it with. Message will be
     * decrypted using a ceaser cipher shifted backwards by key's amount. Method converts message string into a
     * character array, then cycles though array using a for loop. For each character their value is shifted by the
     * key's amount backwards then the decrypted version is added to end of decrypted string variable which is passed
     * back to original method.
     *
     * @param message String, Message to be decrypted
     * @param key int, Key value to be used in ceaser cipher encryption
     * @return String, Decrypted message
     */
    private static String keyDecoding(String message, int key) {
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

            } else if (String.valueOf(messageChar).matches("\\d") ) {
                // Char is numerical
                decodedMessage = decodedMessage.concat(String.valueOf((char) ((int) messageChar - key)));

            } else {
                // Find position of char within alphabet then concat the decoded char to message
                int position = (ALPHABET.indexOf(messageChar) - key) % 26;

                // Circle around to other side of alphabet if position is negative
                if (position < 0) {
                    position = ALPHABET.length() + position;
                }

                decodedMessage = decodedMessage.concat(String.valueOf(
                        ALPHABET.charAt(position))
                );

            }
        }

        return decodedMessage;
    }

}
