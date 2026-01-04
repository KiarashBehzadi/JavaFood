package Exceptions;

public class RestaurantIsClose extends RuntimeException {
    public RestaurantIsClose() {
        super("Restaurant is closed");
    }
}
