package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        // Initialize variables
        byte[] dataBuffer = new byte[65535];
        int interfaceServerPort,
                loginKey, paymentKey;
//        boolean inputValidFlag = false;
        String usernameInput = "", passwordInput = "", encryptedSignIn;

        DatagramPacket sendingPacket, receivingPacket  = new DatagramPacket(dataBuffer, dataBuffer.length);;
        DatagramSocket clientSocket;
        InetAddress interfaceServerAddress;
        Scanner input = new Scanner(System.in);

        // Initialize itemDatabase
            // use list of items

        // Connect to interface server for user verification
        try {
            // Set up connection variables for interfaceServer at IP address "___" and port number "___"
            clientSocket = new DatagramSocket();
            //TODO CHANGE TO INTERFACE SERVER ADDRESS
            interfaceServerAddress = InetAddress.getByName("localhost");
            interfaceServerPort = 3000;

            // Set loginKey to default (4)
            loginKey = 4;

            // Prompt for username and password input, loop if invalid
//            do {
                try {
                    System.out.println("Enter username of account: ");
                    usernameInput = input.next().trim().toUpperCase();
                    System.out.println("Enter password of account: ");
                    passwordInput = input.next().trim().toUpperCase();
                } catch (Exception exception) {}

//                // Verify username and password doesn't contain any whitespace characters
//                if (usernameInput.matches("") && passwordInput.matches("")) {
//                    // Inputs valid, switch flag
//                    inputValidFlag = true;
//                } else {
//                    // Inputs invalid repeat prompt
//                    System.out.println("Error input invalid, whitespace character included");
//                }
//            } while (!inputValidFlag);

            // Create encryptedSignIn from pattern "username" + "_" + "password" using 4 for key
            encryptedSignIn = keyEncoding(usernameInput + "_" + passwordInput, loginKey);

            // Create and send packet for InterfaceServer
            clientSocket.send(new DatagramPacket(encryptedSignIn.getBytes(), encryptedSignIn.getBytes().length,
                    interfaceServerAddress, interfaceServerPort));

            // Receive packet from socket connection to InterfaceServer
            clientSocket.receive(receivingPacket);

            // Split by "_", decode, and store received confirmation and paymentKey

            // Check if confirmation is true

            // Prompt user to choose items from InterfaceServer list and send selection back InterfaceServer

            // Prompt user for payment input

            // Output conformation to user


        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (Exception exception) {}

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