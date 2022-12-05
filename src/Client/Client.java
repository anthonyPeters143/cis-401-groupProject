package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Class: Client, Used to prompt user then communicate connect with InterfaceServer for login info, then item selection,
 * lastly credit card number and security code. Will use port 3000 and localhost till changed for interfaceServer
 * connection.
 *
 * @author Anthony Peters
 */

public class Client {

    /**
     * Method: main, Used to drive Client for client side TCP connection with InterfaceServer. User will be prompted for
     * username and password inputs. Usernames and passwords can only require alphanumerical or underscore characters.
     * Once inputs are verified then they will be placed in pattern "username_password" then encrypted and send to
     * InterfaceServer. If invalid then Client and InterfaceServer will loop till inputs are valid. Once valid then
     * InterfaceServer will send list of item entries to Client. Client will store item entries as LinkedList filled
     * with itemEntryNodes. User will be prompted for item selection and then quantity inputs, if out of range then
     * system will notify user and prompt for another input till valid. If at least one selection is complete then
     * system will prompt user to quit expecting a 'y' or 'n' character reply, if input isn't valid then system will
     * loop till it is. Once selection is valid Client will send selection input in pattern "indexNumber:quantity" to
     * InterfaceServer. If selection is invalid then Client will loop till valid. If selection valid then it will be
     * sent InterfaceServer to respond with receipt which will be posted to user. Then user will input credit card and
     * security code which need to be 16 and 3 characters respectively. Input will be validated and system will loop
     * till valid. When valid input will be placed in pattern "creditCard-securityCode" then encrypted and sent to
     * InterfaceServer. Then Client will wait until it receives conformation value. If conformation value is = 0 then,
     * payment input is invalid and will need to be reentered till valid. If conformation value is = 1 then, payment
     * input is valid and system will output "order compete"
     *
     * @param args System input
     */
    public static void main(String[] args) {
        // Initialize variables
        byte[] dataBuffer;
        int interfaceServerPort,
                loginKey,userAccountKey,
                itemIndexInput, itemQuantityInput;
        boolean inputValidFlag = false, quantityValidFlag= false,
                loginConfirmationFlag = false, itemSelectionFlag = false,
                quitSelectionFlag = false,
                creditCardInputFlag, securityCodeInputFlag,
                paymentValidFlag = false;
        String usernameInput = "", passwordInput = "",
                encryptedSignIn,
                decryptedReport,
                itemListString,
                receiptString,
                decryptedCreditCard, decryptedSecurityCode,
                encryptedPayment;
        String[] loginSplitInput;
        LinkedList<ItemEntryNode> itemEntryNodeLinkedList;
        DatagramPacket receivingPacket;
        DatagramSocket clientSocket;
        InetAddress interfaceServerAddress;
        Scanner input = new Scanner(System.in);

        // Connect to interface server for user verification
        try {
            // Set up connection variables for interfaceServer at IP address and port number 3000
            // TODO CHANGE TO INTERFACE SERVER ADDRESS
            interfaceServerAddress = InetAddress.getByName("localhost");
            interfaceServerPort = 3000;
            clientSocket = new DatagramSocket();

            // Set loginKey to default (4)
            loginKey = 4;

            // Prompt for username and password input, loop if invalid
            do {
                do {
                    try {
                        System.out.print("\nUsername and passwords can include: letters, numbers, and underscores\n" +
                                "Enter username of account: ");
                        usernameInput = input.next().trim().toUpperCase();
                        System.out.print("Enter password of account: ");
                        passwordInput = input.next().trim().toUpperCase();
                    } catch (Exception exception) {}

                    // Verify username and password contain only alphanumeric and underscore characters
                    if (usernameInput.matches("\\w+") && passwordInput.matches("\\w+")) {
                        // Inputs valid, switch flag
                        inputValidFlag = true;
                    } else {
                        // Inputs invalid repeat prompt
                        System.out.println("Input Error, invalid character included");
                    }
                } while (!inputValidFlag);

                // Create encryptedSignIn from pattern "username" + "_" + "password" using loginKey for key
                encryptedSignIn = keyEncoding(usernameInput + "_" + passwordInput, loginKey);

                // Create and send packet for InterfaceServer
                clientSocket.send(new DatagramPacket(encryptedSignIn.getBytes(), encryptedSignIn.getBytes().length,
                        interfaceServerAddress, interfaceServerPort));

                // Receive packet from socket connection to InterfaceServer
                dataBuffer = new byte[65535];
                receivingPacket  = new DatagramPacket(dataBuffer, dataBuffer.length);
                clientSocket.receive(receivingPacket);

                // Split by "_", decode, and store received confirmation and paymentKey
                loginSplitInput = new String(receivingPacket.getData()).trim().split("_");

                // Check conformation
                if (Integer.parseInt(keyDecoding(loginSplitInput[0], loginKey)) == 1) {
                    // Input is valid
                    loginConfirmationFlag = true;
                } else {
                    // Input is invalid
                    System.out.println("Input is invalid, please re-input");
                }

                // Set user account key for payment
                userAccountKey = Integer.parseInt(keyDecoding(loginSplitInput[1], loginKey));

            // Check if confirmation is true
            } while (!loginConfirmationFlag);

            // Receive, separate by "\n" then ":", and store itemList data as itemListString
            dataBuffer = new byte[65535];
            receivingPacket  = new DatagramPacket(dataBuffer, dataBuffer.length);
            clientSocket.receive(receivingPacket);
            itemEntryNodeLinkedList = createItemEntries(new String(receivingPacket.getData()));
            itemListString = createItemListPrompt(itemEntryNodeLinkedList);

            // Prompt and loop till user chooses at least 1 item from InterfaceServer itemList
            System.out.println("\n" + itemListString);
            do {
                try {
                    System.out.print("Enter item index number or -1 for catalog : ");
                    itemIndexInput = Integer.parseInt(input.next().trim());
                } catch (Exception exception) {
                    itemIndexInput = -2;
                }

                // Check input validity
                if (itemIndexInput > itemEntryNodeLinkedList.size() || itemIndexInput < -2) {
                    // itemIndex is invalid
                    System.out.println("Input is out of range, please re-input");
                } else if (itemIndexInput != -2 && itemIndexInput != -1) {
                    // itemIndex is valid
                    // Prompt and loop for quantity
                    do {
                        System.out.print("Enter desired quantity : ");
                        itemQuantityInput = Integer.parseInt(input.next().trim());

                        // Check if input is within range of (1,1000)
                        if (itemQuantityInput >= 1 && itemQuantityInput <= 1000) {
                            quantityValidFlag = true;
                        } else {
                            // itemQuantity is invalid
                            quantityValidFlag = false;
                            System.out.println("Input is out of range, please re-input");
                        }

                    } while (!quantityValidFlag);

                    // Add quantity and priceTotal to choose item entry
                    itemEntryNodeLinkedList.get(itemIndexInput - 1).updatePriceTotalFromQuantity(itemQuantityInput);

                    // Display updated details
                    System.out.println(createItemEntryFromIndexNumber(itemIndexInput - 1, itemEntryNodeLinkedList));
                } else if (itemIndexInput == -1) {
                    // itemIndex is for catalog
                    System.out.println(itemListString);
                } else {
                    // itemIndex is invalid
                    System.out.println("Input is invalid, please re-input");
                }

                // Check if user has chosen 1 item yet then set flag state, false if user has not chosen yet
                if (checkIfUserPicked(itemEntryNodeLinkedList)) {
                    // Loop till input is valid
                    do {
                        // Prompt user if they want to quit
                        System.out.print("Quit selection? (Y/N) : ");
                        String qSelection = input.next().trim().toLowerCase();

                        if (qSelection.matches("y")) {
                            // Input valid, user wants to quit
                            // Set flags
                            quitSelectionFlag = true;
                            itemSelectionFlag = true;
                        } else if (qSelection.matches("n")) {
                            // Input valid, user doesn't want to quit
                            // Set flag
                            quitSelectionFlag = true;
                        } else {
                            // Input invalid
                            System.out.println("Input is invalid, please re-input");
                        }
                    } while (!quitSelectionFlag);
                }
            } while (!itemSelectionFlag);

            // Create encrypted report for InterfaceServer
            decryptedReport = createItemReport(itemEntryNodeLinkedList);

            // Send selection back InterfaceServer
            clientSocket.send(new DatagramPacket(decryptedReport.getBytes(), decryptedReport.getBytes().length,
                    interfaceServerAddress, interfaceServerPort));

            // Receive receipt data
            dataBuffer = new byte[65535];
            receivingPacket  = new DatagramPacket(dataBuffer, dataBuffer.length);
            clientSocket.receive(receivingPacket);

            // Find receipt string and output to user
            receiptString = (createReceipt(new String(receivingPacket.getData())));
            System.out.println("\n" + receiptString);

            // Prompt user for payment input, loop till valid
            do {
                do {
                    try {
                    // Output total and prompt
                    System.out.print("Enter credit card number (16 digits) : ");
                    decryptedCreditCard = input.next().trim();

                    // Check if input is exactly 16 digits
                    if (!(decryptedCreditCard).matches("\\d{16}")) {
                        // Input invalid
                        decryptedCreditCard = "-1";

                        System.out.println("Input is invalid, please re-input");
                    }
                } catch (Exception exception) {
                    // Input invalid
                    decryptedCreditCard = "-1";

                    System.out.println("Input is invalid, please re-input");
                }

                // Check input validity
                creditCardInputFlag = !decryptedCreditCard.equals("-1");
                } while (!creditCardInputFlag);

                do {
                    try {
                    // Output total and prompt
                    System.out.print("Enter security code (3 digits) : ");
                    decryptedSecurityCode = input.next().trim();

                    // Check if input is exactly 3 digits
                    if (!(decryptedSecurityCode).matches("\\d{3}")) {
                        // Input invalid
                        decryptedSecurityCode = "-1";

                        System.out.println("Input is invalid, please re-input");
                    }
                } catch (Exception exception) {
                    // Input invalid
                    decryptedSecurityCode = "-1";

                    System.out.println("Input is invalid, please re-input");
                }

                // Check securityCode input validity
                securityCodeInputFlag = !decryptedSecurityCode.equals("-1");

                } while (!securityCodeInputFlag);

                // Input valid, encrypt data
                encryptedPayment = keyEncoding(decryptedCreditCard + "-" + decryptedSecurityCode, userAccountKey);

                // Send payment data to interface server
                clientSocket.send(new DatagramPacket(encryptedPayment.getBytes(), encryptedPayment.getBytes().length,
                        interfaceServerAddress, interfaceServerPort));

                // Receive conformation
                dataBuffer = new byte[65535];
                receivingPacket  = new DatagramPacket(dataBuffer, dataBuffer.length);
                clientSocket.receive(receivingPacket);

                // Check if payment input is valid
                if ((new String(receivingPacket.getData()).trim()).matches("1")) {
                    // Set paymentValidFlag
                    paymentValidFlag = true;
                } else {
                    // Input invalid
                    System.out.println("Input is invalid, please re-input");
                }

            } while (!paymentValidFlag);

            // Output conformation to user
            System.out.println("Order complete");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (Exception exception) {}
    }

    /**
     * Method: createReceipt, Method takes receipt input from InterfaceServer and splits it by "~" character and
     * returns it.
     *
     * @param receiptString String, receipt input from InterfaceServer
     * @return String, String value of receipt
     */
    private static String createReceipt(String receiptString) {
        // Split string by "~" character for receipt
        String[] tempArray = receiptString.split("~");
        return tempArray[0];
    }

    /**
     * Method: createItemReport, Method takes LinkedList of itemEntryNodes and puts values into "indexNumber:quantity"
     * pattern within a String value that is returned.
     *
     * @param itemListLinkedList LinkedList<ItemEntryNode>, Linked list of itemEntryNodes
     * @return String value of indexNumber and quantity separated by ":"
     */
    private static String createItemReport(LinkedList<ItemEntryNode> itemListLinkedList) {
        // Initialize string
        String itemReport = "";

        // Loop through itemList attaching items in the pattern indexNumber ":" quantity "\n"
        for (int index = 0; index < itemListLinkedList.size(); index++) {
            itemReport = itemReport.concat(itemListLinkedList.get(index).getIndexNumber() +
                    ":" + itemListLinkedList.get(index).getQuantity());

            if (index < itemListLinkedList.size() - 1) {
                itemReport = itemReport.concat("\n");
            }
        }

        return itemReport;
    }

    /**
     * Method: checkIfUserPicked, Method cycles through LinkedList counting quantities for each node then returning
     * boolean value based on if counter variable is greater than 0.
     *
     * @param itemListLinkedList LinkedList<ItemEntryNode>, Linked list of itemEntryNodes
     * @return boolean. value based on if quantity counter is greater than 0
     */
    private static boolean checkIfUserPicked(LinkedList<ItemEntryNode> itemListLinkedList) {
        // Initialize counter
        int itemCounter = 0;

        // Loop through itemList increasing quantities counter
        for (int index = 0; index < itemListLinkedList.size(); index++) {
            itemCounter += itemListLinkedList.get(index).getQuantity();
        }

        return itemCounter > 0;
    }

    /**
     * Method: createItemEntries, Method takes String input from InterfaceServer and creates LinkedList of
     * itemEntryNodes then returns it.
     *
     * @param itemListInput String, String value of all items send from InterfaceServer
     * @return LinkedList<ItemEntryNode>, Linked list of itemEntryNodes
     */
    private static LinkedList<ItemEntryNode> createItemEntries(String itemListInput) {
        String[] itemEntries, entryData;
        LinkedList<ItemEntryNode> itemListLinkedList = new LinkedList<ItemEntryNode>();

        // Split input by "\n" chars
        itemEntries = itemListInput.split("\n");

        // Loop through itemEntries to create itemEntryNodes
        for (int index = 0; index < itemEntries.length - 1; index++) {
            // Split by : chars
            entryData = itemEntries[index].split(":");

            // Create itemEntryNodes from item indexNumber, name, and price
            itemListLinkedList.add(new ItemEntryNode(Integer.parseInt(entryData[0]),
                    entryData[1], Double.parseDouble(entryData[2])));
        }

        return itemListLinkedList;
    }

    /**
     * Method: createItemListPrompt, Method will take LinkedList of itemEntryNodes and create String prompt including
     * all values within itemListNodes.
     *
     * @param itemListLinkedList LinkedList<ItemEntryNode>, Linked list of itemEntryNodes
     * @return String, Prompt of item entries
     */
    private static String createItemListPrompt(LinkedList<ItemEntryNode> itemListLinkedList) {
        // Initialize String
        String itemListString = "#\tName\tPrice\n";
        ItemEntryNode itemEntryNode;

        // Loop through itemList attaching items as pattern "indexNumber    name    price"
        for (int index = 0; index < itemListLinkedList.size(); index++) {
            // Set node
            itemEntryNode = itemListLinkedList.get(index);

            // Add item's indexNumber, name, and price
            itemListString = itemListString.concat(
                    itemEntryNode.getIndexNumber() + "\t" +
                            itemEntryNode.getName() + "\t" +
                            itemEntryNode.getPrice() + "\n");
        }

        // Return output string
        return itemListString;
    }

    /**
     * Method: createItemEntryFromIndexNumber, Method will take LinkedList of itemEntryNodes and indexNumber to search
     * ItemEntryNode using it's indexNumber value. When it is find then method will create item entry prompt including:
     * name, quantity, price, and item total.
     *
     * @param indexNumber int, IndexNumber value for wanted ItemEntryNode
     * @param itemListLinkedList LinkedList<ItemEntryNode>, Linked list of itemEntryNodes
     * @return String, Item prompt
     */
    private static String createItemEntryFromIndexNumber(int indexNumber, LinkedList<ItemEntryNode> itemListLinkedList) {
        // Initialize String
        String itemEntryString = "";

        // Initialize ItemEntryNode from indexNumber
        ItemEntryNode itemEntryNode = itemListLinkedList.get(indexNumber);

        // Create item details (name, price, quantity, and priceTotal) string
        // from item indexNumber - 1 due to different in starting numbers
        itemEntryString = "Name: " + itemEntryNode.getName() +
                "\t\tQuantity : " + itemEntryNode.getQuantity() +
                "\t\tPrice : " + itemEntryNode.getPrice() +
                "\t\tItem Total : " + itemEntryNode.getPriceTotal();

        return itemEntryString;
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