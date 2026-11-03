package zm.gearlog.exception;

/** Thrown when someone tries to lend an item that is already out. */
public class ItemAlreadyLentException extends RuntimeException {
    public ItemAlreadyLentException(String itemName) {
        super(itemName + " is already lent out. Wait for it to be returned.");
    }
}
