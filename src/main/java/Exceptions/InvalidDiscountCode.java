package Exceptions;

public class InvalidDiscountCode extends RuntimeException {
    public InvalidDiscountCode(String message) {
        super(message);
    }
}
