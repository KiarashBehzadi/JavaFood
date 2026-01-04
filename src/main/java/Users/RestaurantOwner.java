package Users;
import Models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantOwner extends User {
    private List<Restaurant> restaurants = new ArrayList<>();

    public RestaurantOwner(int id, String name) {
        super(id, name);
    }
}