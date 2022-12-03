package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        // Initialize variables
        byte[] dataBuffer = new byte[65535];
        int interfaceServerPort,
                loginKey, paymentKey,
                userAccountKey,
                itemListSize,
                itemIndexInput, itemQuantityInput;
        boolean inputValidFlag = false, loginConfirmationFlag = false, itemSelectionFlag = false,
                quitSelectionFlag = false,
                creditCardInputFlag = false, securityCodeInputFlag = false,
                paymentValidFlag = false;
        double receiptTotal;
        String usernameInput = "", passwordInput = "",
                encryptedSignIn,
                encryptedReport, decryptedReport,
                itemListString,
                receiptString,
                decryptedCreditCard, decryptedSecurityCode,
                encryptedPayment;
        String[] loginSplitInput, itemSplitInput;

        LinkedList<itemEntryNode> itemEntryNodeLinkedList = new LinkedList<itemEntryNode>();

        DatagramPacket sendingPacket, receivingPacket  = new DatagramPacket(dataBuffer, dataBuffer.length);;
        DatagramSocket clientSocket;
        InetAddress interfaceServerAddress;
        Scanner input = new Scanner(System.in);

        // Initialize itemDatabase
            // use list of items

        // Connect to interface server for user verification
        try {
            // Set up connection variables for interfaceServer at IP address "___" and port number "___"

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
                clientSocket.receive(receivingPacket);

                // Split by "_", decode, and store received confirmation and paymentKey
                loginSplitInput = new String(receivingPacket.getData()).trim().split("_");
                if (Integer.parseInt(keyDecoding(loginSplitInput[0], loginKey)) == 1) {
                    loginConfirmationFlag = true;
                }
                userAccountKey = Integer.parseInt(keyDecoding(loginSplitInput[1], loginKey));

            // Check if confirmation is true
            } while (!loginConfirmationFlag);

            // Receive, separate by "\n" then ":", and store itemList data as itemListString
            clientSocket.receive(receivingPacket);
            itemEntryNodeLinkedList = createItemEntries(new String(receivingPacket.getData()));
            itemListString = createItemListPrompt(itemEntryNodeLinkedList);

            // Prompt and loop till user chooses at least 1 item from InterfaceServer itemList
            System.out.println(itemListString);
            do {
                try {
                    System.out.print("Enter item index number or -1 for catalog : ");
                    itemIndexInput = Integer.parseInt(input.next().trim());

                } catch (Exception exception) {
                    itemIndexInput = -2;

                }

                if (itemIndexInput > itemEntryNodeLinkedList.size() || itemIndexInput < -2) {
                    // itemIndex is invalid
                    System.out.println("Input is out of range, please re-input");

                } else if (itemIndexInput != -2 && itemIndexInput != -1) {
                    // itemIndex is valid
                    // Prompt for quantity
                    System.out.print("Enter desired quantity : ");
                    itemQuantityInput = Integer.parseInt(input.next().trim());

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

                    do {
                        // Prompt user if they want to quit
                        System.out.print("Quit selection? (Y/N) : ");
                        String qSelection = input.next().trim().toLowerCase();

                        if (qSelection.matches("y")) {
                            // Input valid, user wants to quit
                            // Set flags
                            quitSelectionFlag = true;
                            itemSelectionFlag = true;

                        } if (qSelection.matches("n")) {
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
//            encryptedReport = keyEncoding(createItemReport(itemEntryNodeLinkedList), userAccountKey);
            decryptedReport = createItemReport(itemEntryNodeLinkedList);

            // Send selection back InterfaceServer
            clientSocket.send(new DatagramPacket(decryptedReport.getBytes(), decryptedReport.getBytes().length,
                    interfaceServerAddress, interfaceServerPort));

            // Receive receipt data
            clientSocket.receive(receivingPacket);

            // Find receipt string and total
            receiptString = (createReceipt(new String(receivingPacket.getData())));
            receiptTotal = findReceiptTotal(receiptString);

            // TODO TESTING DOWN FROM HERE
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
                    }

                } catch (Exception exception) {
                    // Input invalid
                    decryptedCreditCard = "-1";
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
                    }

                } catch (Exception exception) {
                    // Input invalid
                    decryptedSecurityCode = "-1";
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
                clientSocket.receive(receivingPacket);

                // Check if payment input is valid
                if (new String(receivingPacket.getData()).matches("1")) {
                    // Set paymentValidFlag
                    paymentValidFlag = true;
                }

            } while (paymentValidFlag);

            // Output conformation to user
            System.out.println("Order complete");


        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (Exception exception) {}

    }

    private static String createReceipt(String receiptString) {
        // Split string by "~" character for receipt
        String[] tempArray = receiptString.split("~");
        return tempArray[0];
    }

    private static double findReceiptTotal(String receiptString) {
        // Split by ":" then find total by substring method
        String[] tempArray = receiptString.split(":");
        return Double.parseDouble(tempArray[1].substring(2));
    }

    private static String createItemReport(LinkedList<itemEntryNode> itemListLinkedList) {
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

    private static boolean checkIfUserPicked(LinkedList<itemEntryNode> itemListLinkedList) {
        // Initialize counter
        int itemCounter = 0;

        // Loop through itemList increasing quantities counter
        for (int index = 0; index < itemListLinkedList.size(); index++) {
            itemCounter += itemListLinkedList.get(index).getQuantity();
        }

        return itemCounter > 0;
    }

    private static LinkedList<itemEntryNode> createItemEntries(String itemListInput) {
        String[] itemEntries, entryData;
        LinkedList<itemEntryNode> itemListLinkedList = new LinkedList<itemEntryNode>();

        // Split input by "\n" chars
        itemEntries = itemListInput.split("\n");

        // Loop through itemEntries to create itemEntryNodes
        for (int index = 0; index < itemEntries.length - 1; index++) {
            // Split by : chars
            entryData = itemEntries[index].split(":");

            // Create itemEntryNodes from item indexNumber, name, and price
            itemListLinkedList.add(new itemEntryNode(Integer.parseInt(entryData[0]),
                    entryData[1], Double.parseDouble(entryData[2])));
        }

        return itemListLinkedList;
    }

    private static String createItemListPrompt(LinkedList<itemEntryNode> itemListLinkedList) {
        // Initialize String
        String itemListString = "#\tName\tPrice\n";
        itemEntryNode itemEntryNode;

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

    private static String createItemEntryFromIndexNumber(int indexNumber, LinkedList<itemEntryNode> itemListLinkedList) {
        // Initialize String
        String itemEntryString = "";
        String[] itemEntry;

        // Initialize itemEntryNode from indexNumber
        itemEntryNode itemEntryNode = itemListLinkedList.get(indexNumber);

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