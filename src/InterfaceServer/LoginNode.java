package InterfaceServer;

/**
 * Class: LoginNode, Used to hold values as nodes within LinkedList database. Will store username, password, and key
 * values.
 *
 * @author Anthony Peters
 */

public class LoginNode {
    private String username, password;
    private int key;

    /**
     * Method: LoginNode constructor, Augment-constructor that needs username and password string inputs and key int
     * input. Will store values in username, password, and key fields.
     *
     * @param usernameInput String, Username to be stored
     * @param passwordInput String, Password to be stored
     * @param keyInput int, Key int value to be stored
     */
    LoginNode(String usernameInput, String passwordInput, int keyInput) {
        username = usernameInput;
        password = passwordInput;
        key = keyInput;
    }

    /**
     * Method: getUsername, Returns String value of username parameter
     *
     * @return String, Username value of LoginNode
     */
    public String getUsername() {
        return username;
    }

    /**
     * Method: getPassword, Returns String value of password parameter
     *
     * @return String, Last name value of LoginNode
     */
    public String getPassword() {
        return password;
    }

    /**
     * Method: getSsn, Returns int value of key parameter
     *
     * @return int, Key value of LoginNode
     */
    public int getKey() {
        return key;
    }

}
