package Exceptions;

public class InvalidScore extends RuntimeException {
    public InvalidScore(String message) {
        super(message);
    }
}
