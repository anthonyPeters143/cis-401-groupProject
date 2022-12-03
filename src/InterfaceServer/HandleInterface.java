package InterfaceServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

public class HandleInterface {
    int loginKey,
        clientPort, interfaceSeverPort, dataServerPort,
        accountKey;

    double userTotal;

    String userReceipt;

    String usernameEncrypted, usernameDecrypted,
            passwordEncrypted, passwordDecrypted,
            selectionEncrypted, getSelectionEncrypted;

    String smInvalidLogin;

    DatagramPacket clientPacket;
    DatagramSocket interfaceSocket;
    InetAddress clientAddress;
    LinkedList<LoginNode> loginNodeLinkedList;
    LinkedList<CatalogNode> catalogNodeLinkedList;


    HandleInterface(DatagramPacket clientPacketInput, int loginKeyInput,
                    DatagramSocket interfaceSocketInput, LinkedList<LoginNode> loginNodeLinkedListInput,
                    LinkedList<CatalogNode> catalogNodeLinkedListInput) {
        // Initialize fields
        clientPacket = clientPacketInput;
        clientPort = clientPacketInput.getPort();
        clientAddress = clientPacketInput.getAddress();
        loginKey = loginKeyInput;
        interfaceSocket = interfaceSocketInput;
        loginNodeLinkedList = loginNodeLinkedListInput;
        catalogNodeLinkedList = catalogNodeLinkedListInput;

        // Initialize system messages
        smInvalidLogin = "Username or password input is invalid or not found, please retry";


        // Initialize connection to DataServer TODO

    }


    public String getDataLogin() {
        return usernameDecrypted + ":" + passwordDecrypted;
    }

    public void createReceipt() {
        // Initialize string with header
        String receipt = "User Receipt\n#\tName\tPrice\tQuant\tItem Total\n";
        CatalogNode catalogNode;

        // Loop through catalogNode concatting string containing item's: indexNumber, name,
        // price, quantity, and item total
        for (int index = 0; index < catalogNodeLinkedList.size(); index++) {
            // Set catalogNode
            catalogNode = catalogNodeLinkedList.get(index);

            receipt = receipt.concat(
                    catalogNode.getIndexNumber() + "\t" +
                            catalogNode.getName() + "\t" +
                            catalogNode.getPrice() + "\t\t" +
                            catalogNode.getQuantity() + "\t\t$" +
                            catalogNode.getPrice()) + "\n";

        }

        // Find total
        userTotal = updateTotal();

        // Add total
        receipt = receipt.concat("\tTotal : $" + userTotal + "~");

        userReceipt = receipt;
    }

    private double updateTotal() {
        // Initialize total counter
        double totalCounter = 0;

        // Loop through list to add to counter
        for (int index = 0; index < catalogNodeLinkedList.size(); index++) {
            totalCounter += catalogNodeLinkedList.get(index).getPrice();
        }

        return totalCounter;
    }

    // TODO EXCEPTION
    public void updateSelection() {
        // Create string from packet data, Split by "\n" to separate index entries
        String[] itemSelectionString = new String(clientPacket.getData()).split("\n");
//                keyDecoding(new String(clientPacket.getData()),accountKey).split("\n");
        String[] itemSelectionEntry;

        // Loop through array of index entries adding quantities to entries nodes
        for (int index = 0; index < itemSelectionString.length; index++) {
            // Split by ":" to separate indexNumber from quantity
            itemSelectionEntry = itemSelectionString[index].trim().split(":");

            // Add quantities and totals to item nodes
            catalogNodeLinkedList.get(Integer.parseInt(itemSelectionEntry[0]) - 1).
                    updatePriceTotalFromQuantity(Integer.parseInt(itemSelectionEntry[1]));
        }

        // Update receipt
        createReceipt();
    }

    // Attempt to log in into account using username and password, encoded using loginKey value. Will decrypt inputs
    // then check if inputs match any LoginNodes. If found to match then accountKey will be recorded for later use.
    public boolean login() {
        try {
            // Spilt input by "_" into encrypted username and password
            String[] splitInput = (new String(clientPacket.getData()).trim()).split("_");
            usernameDecrypted = keyDecoding(splitInput[0], loginKey).toUpperCase();
            passwordDecrypted = keyDecoding(splitInput[1], loginKey).toUpperCase();

        } catch (Exception exception) {
            // If exception occurs then set both username and password to null
            usernameDecrypted = null;
            passwordDecrypted = null;
        }

        // If inputs aren't null
        if (usernameDecrypted != null && passwordDecrypted != null) {
            // Try login with username and password and set accountKey
            if (attemptLogin(usernameDecrypted, passwordDecrypted) != -1) {
                // Input valid
                return true;
            }
        }

        return false;
    }

    // Will attempt to log in using username and password inputs. If matching then accountKey value will be recorded,
    // if not then accountKey will be set to -1.
    private int attemptLogin(String usernameInput, String passwordInput) {
        // Cycle through loginNodeLinkedList to match inputs to LoginNode fields
        for (int index = 0; index < loginNodeLinkedList.size(); index++) {
            // Test if username and password match LoginNode
            if (loginNodeLinkedList.get(index).getUsername().matches(usernameInput) &&
            loginNodeLinkedList.get(index).getPassword().matches(passwordInput)) {
                // Username and password match Node, set and return index number
                accountKey = loginNodeLinkedList.get(index).getKey();
                return accountKey;
            }
        }

        // No LoginNodes contain matching username and password set and return "-1" as index number
        accountKey = -1;
        return accountKey;
    }

    // Updates the client packet in thread
    public void updateClientPacket(DatagramPacket newPacket) {
        clientPacket = newPacket;
    }

    // Returns encoded login update message using login key
    public String getLoginResult(boolean result) {
        String loginResult = "";

        // Attach boolean flag for result
        if (result) {
            loginResult = loginResult.concat("1_");
        } else {
            loginResult = loginResult.concat("0_");
        }

        // Attach accountKey
        loginResult = loginResult.concat(String.valueOf(accountKey));

        return keyEncoding(loginResult, loginKey);
    }



    // Loops through CatalogNodeLinkedList and creates a string including indexNumber, name, and price. Will use
    // pattern indexNumber + ":" + name + ":" + price "\n", will skip new line on last line.
    private static String prepItemList(LinkedList<CatalogNode> itemList) {
        // Initialize String
        String itemListOutput = "";

        // Loop through itemList attaching item as pattern "indexNumber-"
        for (int index = 0; index < itemList.size(); index++) {
            // Add item's indexNumber, name, and price
            itemListOutput = itemListOutput.concat(
                    itemList.get(index).getIndexNumber() + ":" +
                            itemList.get(index).getName() + ":" +
                            itemList.get(index).getPrice());

            // If not last loop then add \n character
            if (index != itemList.size()) {
                itemListOutput = itemListOutput.concat("\n");
            }
        }

        // Return output string
        return itemListOutput;
    }

    public String getUserReceipt() {
        return userReceipt;
    }

    // Calls method to convert CatalogNodeLinkedList into a String, encoding using account key and returns it
    public String getPreppedItemList() {
        // TODO SWITCH TO CREATE IN CONSTRUCTOR
        return prepItemList(catalogNodeLinkedList);
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
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
