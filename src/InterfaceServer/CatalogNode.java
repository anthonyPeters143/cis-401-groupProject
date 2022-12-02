package InterfaceServer;

/**
 * Class: CatalogNode, Used to hold values as nodes within LinkedList database. Will store indexNumber, name, and price
 * values.
 *
 * @author Anthony Peters
 */

public class CatalogNode {
    private int indexNumber, quantity;
    private double price, priceTotal;
    private String name;

    /**
     * Method: CatalogNode constructor, Augment-constructor that needs username and password string inputs and key int
     * input. Will store values in username, password, and key fields.
     *
     * @param indexNumberInput int, Index value of item to be stored
     * @param priceInput double, Price of item to be stored
     * @param nameInput String, Name of item to be stored
     */
    CatalogNode(int indexNumberInput, String nameInput, double priceInput) {
        indexNumber = indexNumberInput;
        price = priceInput;
        name = nameInput;
        quantity = 0;
        priceTotal = 0;
    }

    void updatePriceTotalFromQuantity(int quantityInput) {
        priceTotal += price * quantityInput;
        quantity += quantityInput;
    }

    /**
     * Method: getIndexNumber, Returns int value of indexNumber parameter
     *
     * @return int, IndexNumber value of CatalogNode
     */
    public int getIndexNumber() {
        return indexNumber;
    }

    /**
     * Method: getPrice, Returns double value of price parameter
     *
     * @return double, Price value of LoginNode
     */
    public double getPrice() {
        return price;
    }

    /**
     * Method: getName, Returns String value of key parameter
     *
     * @return String, Name value of CatalogNode
     */
    public String getName() {
        return name;
    }

    public double getPriceTotal() {
        return priceTotal;
    }

    public int getQuantity() {
        return quantity;
    }

}