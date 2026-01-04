package Exceptions;

public class FoodNotFound extends RuntimeException {
    public FoodNotFound() {
      super("Food not exists");
    }
}
