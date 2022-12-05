package DataServer;

/**
 * Class: PaymentNode, Class used to store username, password, creditCard, securityCode, and key inputs of users for
 * later use by DataServer.
 *
 * @author Anthony Peters
 */
public class PaymentNode {
    private String username, password, creditCard, securityCode;
    private int key;

    /**
     * Method: PaymentNode constructor, Augment-constructor that takes String inputs of username, password, creditCard,
     * securityCode and int input of userKey. Will store all values within username, password, creditCard, securityCode,
     * and key fields.
     *
     * @param usernameInput String, Username input
     * @param passwordInput String, Password input
     * @param keyInput int, User key input
     * @param creditCardInput String, CreditCard input
     * @param securityCodeInput String, SecurityCode input
     */
    PaymentNode(String usernameInput, String passwordInput, int keyInput, String creditCardInput,
                String securityCodeInput) {
        username = usernameInput;
        password = passwordInput;
        key = keyInput;
        creditCard = creditCardInput;
        securityCode = securityCodeInput;
    }

    /**
     * Method: getUsername, Returns String value of username parameter.
     *
     * @return String, Username value of LoginNode
     */
    public String getUsername() {
        return username;
    }

    /**
     * Method: getPassword, Returns String value of password parameter.
     *
     * @return String, Last name value of LoginNode
     */
    public String getPassword() {
        return password;
    }

    /**
     * Method: getSsn, Returns int value of key parameter.
     *
     * @return int, Key value of LoginNode
     */
    public int getKey() {
        return key;
    }

    /**
     * Method: getCreditCard, Returns String value of creditCard parameter.
     *
     * @return String, CreditCard value of PaymentNode
     */
    public String getCreditCard() {
        return creditCard;
    }

    /**
     * Method: getSecurityCode, Returns String value of securityCode parameter.
     *
     * @return String, SecurityCode value of PaymentNode
     */
    public String getSecurityCode() {
        return securityCode;
    }
}
