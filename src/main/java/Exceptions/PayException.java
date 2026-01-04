package Exceptions;

public class PayException extends RuntimeException {
    public PayException() {
        super("Paying amount is not correct");
    }
}
