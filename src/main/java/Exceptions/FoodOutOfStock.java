package Exceptions;

public class FoodOutOfStock extends RuntimeException {
    public FoodOutOfStock() {
        super("Food out of stock");
    }
}
