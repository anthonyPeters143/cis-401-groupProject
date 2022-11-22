package InterfaceServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

public class HandleInterface {
    int loginKey,
        clientPort, interfaceSeverPort, dataServerPort,
        accountIndex;
    String usernameEncrypted, usernameDecrypted,
            passwordEncrypted, passwordDecrypted;

    DatagramPacket clientPacket;
    DatagramSocket interfaceSocket;
    InetAddress clientAddress;
    LinkedList<LoginNode> loginNodeLinkedList;


    HandleInterface(DatagramPacket clientPacketInput, int loginKeyInput,
                    DatagramSocket interfaceSocketInput, LinkedList<LoginNode> loginNodeLinkedListInput) {
        // Initialize fields
        clientPacket = clientPacketInput;
        clientPort = clientPacketInput.getPort();
        clientAddress = clientPacketInput.getAddress();
        loginKey = loginKeyInput;
        interfaceSocket = interfaceSocketInput;
        loginNodeLinkedList = loginNodeLinkedListInput;


        // Initialize connection to DataServer TODO

    }

    // Attempt to log in into account using username and password, encoded using loginKey value. Will decrypt inputs
    // then check if inputs match any LoginNodes. If found to match then accountIndex will be recorded for later use.
    public boolean login() {
        try {
            // Spilt input by "_" into encrypted username and password
            String[] splitInput = (new String(clientPacket.getData()).trim()).split("_");
            usernameDecrypted = keyDecoding(splitInput[0], loginKey).toLowerCase();
            passwordDecrypted = keyDecoding(splitInput[1], loginKey).toLowerCase();

        } catch (Exception exception) {
            // If exception occurs then set both username and password to null
            usernameDecrypted = null;
            passwordDecrypted = null;
        }

        // If inputs aren't null
        if (usernameDecrypted != null && passwordDecrypted != null) {
            // Try login with username and password
            if (attemptLogin(usernameDecrypted, passwordDecrypted) != -1) {
                return true;
            }
        }

        return false;
    }

    // Will attempt to log in using username and password inputs. If matching then accountIndex value will be recorded,
    // if not then accountIndex will be set to -1.
    private int attemptLogin(String usernameInput, String passwordInput) {
        // Cycle through loginNodeLinkedList to match inputs to LoginNode fields
        for (int index = 0; index < loginNodeLinkedList.size(); index++) {
            // Test if username and password match LoginNode
            if (loginNodeLinkedList.get(index).getUsername().matches(usernameInput) &&
            loginNodeLinkedList.get(index).getPassword().matches(passwordInput)) {
                // Username and password match Node, set and return index number
                accountIndex = index;
                return accountIndex;
            }
        }

        // No LoginNodes contain matching username and password set and return "-1" as index number
        accountIndex = -1;
        return accountIndex;
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
