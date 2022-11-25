package Client;

public class itemEntryNode {

    private int indexNumber, quantity;
    private double price, priceTotal;
    private String name;

    /**
     * Method: itemEntryNode constructor, Augment-constructor that needs name String, indexNumber int, and price double
     * inputs. Will store values in name, indexNumber, and price. Will also store 0 in quantity and priceTotal fields.
     *
     * @param indexNumberInput int, Index value of item to be stored
     * @param priceInput double, Price of item to be stored
     * @param nameInput String, Name of item to be stored
     */
    itemEntryNode(int indexNumberInput, String nameInput, double priceInput) {
        indexNumber = indexNumberInput;
        price = priceInput;
        name = nameInput;
        quantity = 0;
        priceTotal = 0;
    }

    void UpdatePriceTotalFromQuantity(double quantityInput) {
        priceTotal += price * quantityInput;
        quantity += quantityInput;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

}
