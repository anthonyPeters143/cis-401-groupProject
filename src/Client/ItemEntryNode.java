package Client;

/**
 * Class: ItemEntryNode, Class used to store indexNumber, quantity, price, priceTotal, and name of items, used by
 * Client.
 *
 * @author Anthony Peters
 */
public class ItemEntryNode {
    private int indexNumber, quantity;
    private double price, priceTotal;
    private String name;

    /**
     * Method: ItemEntryNode constructor, Augment-constructor that needs name String, indexNumber int, and price double
     * inputs. Will store values in name, indexNumber, and price. Will also store 0 in quantity and priceTotal fields.
     *
     * @param indexNumberInput int, Index value of item to be stored
     * @param priceInput double, Price of item to be stored
     * @param nameInput String, Name of item to be stored
     */
    ItemEntryNode(int indexNumberInput, String nameInput, double priceInput) {
        indexNumber = indexNumberInput;
        price = priceInput;
        name = nameInput;
        quantity = 0;
        priceTotal = 0;
    }

    /**
     * Method: updatePriceTotalFromQuantity, Method uses inputted quantity and multiples by price and stores value in
     * priceTotal and adds quantity to quantity counter,
     *
     * @param quantityInput int, Quantity input
     */
    void updatePriceTotalFromQuantity(int quantityInput) {
        priceTotal += price * quantityInput;
        quantity += quantityInput;
    }

    /**
     * Method: getIndexNumber, Method returns indexNumber value of ItemEntryNode
     *
     * @return int, IndexNumber value of ItemEntryNode,
     */
    public int getIndexNumber() {
        return indexNumber;
    }

    /**
     * Method: getName, Method returns name value of ItemEntryNode
     *
     * @return String, name value of ItemEntryNode
     */
    public String getName() {
        return name;
    }

    /**
     * Method: getPrice, Method returns price value of ItemEntryNode
     *
     * @return double, Price value of ItemEntryNode
     */
    public double getPrice() {
        return price;
    }

    /**
     * Method: getPriceTotal, Method returns priceTotal value of ItemEntryNode
     *
     * @return double, priceTotal value of ItemEntryNode
     */
    public double getPriceTotal() {
        return priceTotal;
    }

    /**
     * Method: getQuantity, Method returns quantity value of ItemEntryNode
     *
     * @return int, Quantity value of ItemEntryNode
     */
    public int getQuantity() {
        return quantity;
    }
}
