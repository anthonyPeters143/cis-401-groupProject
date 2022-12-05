package InterfaceServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * Class: HandleInterface, Class created by InterfaceServer used to store and handle user connection. HandleInterface
 * will store Datagram data, create item list and receipts, and attempt to log in users.
 *
 * @author Anthony Peters
 */
public class HandleInterface {
    int loginKey, clientPort, accountKey;
    double userTotal;
    String preppedItemList, userReceipt;
    String usernameDecrypted, passwordDecrypted;
    DatagramPacket clientPacket;
    DatagramSocket interfaceSocket;
    InetAddress clientAddress;
    LinkedList<LoginNode> loginNodeLinkedList;
    LinkedList<CatalogNode> catalogNodeLinkedList;

    /**
     * Method: HandleInterface constructor, Augment constructor will accept Client DatagramPacket, userKey int value,
     * InterfaceServer DatagramSocket, LoginNode and CatalogNode LinkedLists. Will Store values into clientPacket,
     * clientPort, clientAddress, loginKey, InterfaceSocket, loginNodeLinkedList, and catalogNodeLinkedList. Then create
     * a String value of item list in pattern "indexNumber:name:price" for later use.
     *
     * @param clientPacketInput DatagramPacket, Client packet input
     * @param loginKeyInput int, Login key input
     * @param interfaceSocketInput DatagramSocket, InterfaceServer socket input
     * @param loginNodeLinkedListInput LinkedList<LoginNode>, LinkedList of LoginNodes
     * @param catalogNodeLinkedListInput LinkedList<CatalogNode>, LinkedList of CatalogNodes
     */
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

        // Prep item list String
        preppedItemList = prepItemList(catalogNodeLinkedList);
    }

    /**
     * Method: getDataLogin, Method will create String of username and password values in the pattern
     * "username:password" then return it.
     *
     * @return String value of "username:password"
     */
    public String getDataLogin() {
        return usernameDecrypted + ":" + passwordDecrypted;
    }

    /**
     * Method: createReceipt, Method will create and store String value of receipt including titles
     * "User Receipt   Name    Price   Quant   Item Total" by looping through catalogNodeLinkedList concatting values to
     * end of receipt String value.
     */
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
                            catalogNode.getPrice()) + "\n";}

        // Find total
        userTotal = updateTotal();

        // Add total
        receipt = receipt.concat("\tTotal : $" + userTotal + "~");

        userReceipt = receipt;
    }

    /**
     * Method: updateTotal, Method will loop through catalogNodeLinkedList checking price totals then returning
     * totalCounter values.
     *
     * @return double, Total counter value
     */
    private double updateTotal() {
        // Initialize total counter
        double totalCounter = 0;

        // Loop through list to add to counter
        for (int index = 0; index < catalogNodeLinkedList.size(); index++) {
            totalCounter += catalogNodeLinkedList.get(index).getPrice();
        }

        return totalCounter;
    }

    /**
     * Method: updateSelection, Method will split clientPacket data by "\n" characters then update each
     * catalogNodeLinkedList. Once update is complete then system will update receipt.
     */
    public void updateSelection() {
        // Create string from packet data, Split by "\n" to separate index entries
        String[] itemSelectionString = new String(clientPacket.getData()).split("\n");
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

    /**
     * Method: login, Method will attempt to log in into account using username and password, encoded using loginKey
     * value. Will decrypt inputs then check if inputs match any LoginNodes. If found to match then accountKey will be
     * recorded for later use and system will return true but if not found then system will return false.
     *
     * @return boolean, Result if loginDetails are valid
     */
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

        // Return false if invalid
        return false;
    }

    /**
     * Method: attemptLogin, Method will attempt to log in using username and password inputs to every LoginNode in
     * LinkedList. If found then account key from LoginNode will be recorded and returned, if not found then account key
     * will be recorded and returned as -1.
     *
     * @param usernameInput String, Username input
     * @param passwordInput String, Password input
     * @return int, Account key value from LoginNode
     */
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

    /**
     * Method: updateClientPacket, Method updates the client packet parameter stored.
     *
     * @param newPacket DatagramPacket, New packet input
     */
    public void updateClientPacket(DatagramPacket newPacket) {
        clientPacket = newPacket;
    }

    // Returns encoded login update message using login key

    /**
     * Method: getLoginResult, Method will create encoded using loginKey string in pattern "result_accountKey".
     *
     * @param result boolean, Result of login test
     * @return String value of "result_accountKey"
     */
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

    /**
     * Method: prepItemList, Method loops through CatalogNodeLinkedList and creates a string use pattern
     * "indexNumber:name:price" including every CatalogNode's indexNumber, name, and price.
     *
     * @param itemList LinkedList<CatalogNode>, LinkedList of CatalogNodes
     * @return String value of "indexNumber:name:price"
     */
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

    /**
     * Method: getUserReceipt, Method returns String value of userReceipt from HandleInterface.
     *
     * @return String value of userReceipt
     */
    public String getUserReceipt() {
        return userReceipt;
    }

    /**
     * Method: getPreppedItemList, Method returns String value of preppedItemList from HandleInterface.
     *
     * @return String value of preppedItemList
     */
    public String getPreppedItemList() {
        return preppedItemList;
    }

    /**
     * Method: getClientAddress, Method returns InetAddress value of clientAddress from HandleInterface.
     *
     * @return InetAddress value of clientAddress
     */
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    /**
     * Method: getClientPort, Method returns int value of clientPort from HandleInterface.
     *
     * @return int value of clientPort
     */
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
