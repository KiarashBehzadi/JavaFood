package JavaFood;


import Models.*;
import Users.RestaurantOwner;
import Users.User;
import com.google.gson.*;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class AdminPanel {
    public static LocalDate todayDate;
    public static ArrayList<Restaurant> restaurants = new ArrayList<>();
    public static ArrayList<User> users = new ArrayList<>();
    public static ArrayList<Discount> discounts = new ArrayList<>();
    public static ArrayList<Order> orders = new ArrayList<>();


    /**
     * Loads all data from a JSON file
     *
     * The JSON file should contain:
     * -todayDate
     * -restaurants
     * -users
     * -discounts
     * -orders
     * @param fileAddress path to the JSON file
     */
    public void loadFromJSONFile(String fileAddress) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(fileAddress)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // read today date
            setDate(LocalDate.parse(jsonObject.get("todayDate").getAsString()));

            // create Restaurants
            JsonArray restaurants = jsonObject.getAsJsonArray("restaurants");
            for (JsonElement restaurant : restaurants) {
                JsonObject restObj = restaurant.getAsJsonObject();
                Restaurant r = new Restaurant(restObj.get("id").getAsInt(), restObj.get("name").getAsString(), restObj.get("address").getAsString(),
                        restObj.get("openHour").getAsInt(), restObj.get("closeHour").getAsInt(),
                        Restaurant.RestaurantTypes.valueOf(restObj.get("type").getAsString()));

                if (restObj.has("isOpen") && restObj.get("isOpen").getAsBoolean()) {
                    r.openRestaurant();
                }

                JsonArray foods = restObj.getAsJsonArray("foods");
                for (JsonElement food : foods) {
                    JsonObject foodObj = food.getAsJsonObject();
                    Food f = new Food(foodObj.get("id").getAsInt(), foodObj.get("name").getAsString(), foodObj.get("price").getAsDouble());
                    r.addFood(f, foodObj.get("quantity").getAsInt());
                }

                addRestaurant(r);
            }

            // access Users
            JsonObject users = jsonObject.getAsJsonObject("users");
            JsonArray restaurantOwners = users.getAsJsonArray("restaurantOwners");
            JsonArray admins = users.getAsJsonArray("admins");
            JsonArray customers = users.getAsJsonArray("customers");

            // add restaurant Owners
            for (JsonElement owner : restaurantOwners) {
                JsonObject rOObj = owner.getAsJsonObject();
                RestaurantOwner ro = new RestaurantOwner(rOObj.get("id").getAsInt(), rOObj.get("name").getAsString());
                addUser(ro);
            }

            // add admins
            for (JsonElement admin : admins) {
                JsonObject adminObj = admin.getAsJsonObject();
                RestaurantOwner a = new RestaurantOwner(adminObj.get("id").getAsInt(), adminObj.get("name").getAsString());
                addUser(a);
            }

            // add customers
            for (JsonElement customer : customers) {
                JsonObject customerObj = customer.getAsJsonObject();
                RestaurantOwner c = new RestaurantOwner(customerObj.get("id").getAsInt(), customerObj.get("name").getAsString());
                addUser(c);
            }

            // add discounts
            JsonArray discounts = jsonObject.getAsJsonArray("discounts");
            for (JsonElement discount : discounts) {
                JsonObject discObj = discount.getAsJsonObject();
                if (discObj.has("amount")) {
                    Discount d = new Discount(discObj.get("id").getAsInt(), discObj.get("code").getAsString(),
                            discObj.get("amount").getAsDouble(), LocalDate.parse(discObj.get("expireDate").getAsString()), discObj.get("userId").getAsInt());
                    addDiscount(d);
                }
                else {
                    Discount d = new Discount(discObj.get("id").getAsInt(), discObj.get("code").getAsString(),
                            discObj.get("percentage").getAsInt(), LocalDate.parse(discObj.get("expireDate").getAsString()), discObj.get("userId").getAsInt());
                    addDiscount(d);
                }
            }

            // add orders
            JsonArray orders = jsonObject.getAsJsonArray("orders");
            for (JsonElement order : orders) {
                JsonObject orderObj = order.getAsJsonObject();
                Order o = createOrder(orderObj.get("id").getAsInt(), orderObj.get("userId").getAsInt(), orderObj.get("restaurantId").getAsInt(), Order.ReceivingType.valueOf(orderObj.get("receivingType").getAsString()));

                // add foods to order
                JsonArray foods = orderObj.getAsJsonArray("foods");
                for (JsonElement food : foods) {
                    JsonObject foodObj = food.getAsJsonObject();
                    o.addFoodToOrder(foodObj.get("foodId").getAsInt(), foodObj.get("quantity").getAsInt());
                }

                // add discount codes
                if (orderObj.has("discountCodes")){
                    JsonArray discountCodes = orderObj.getAsJsonArray("discountCodes");
                    for (JsonElement discountCode : discountCodes) {
                        o.addDiscount(discountCode.getAsString());
                    }
                }

                // pay order
                if (orderObj.has("isPaid") && orderObj.get("isPaid").getAsBoolean()) {
                    o.pay(orderObj.get("payAmount").getAsDouble());
                }

                // score order
                if (orderObj.has("score")) {
                    o.scoreOrder(orderObj.get("score").getAsInt());
                }
            }

        } catch (IOException | JsonSyntaxException e) {
//            e.printStackTrace();
        }
    }


    /**
     * Adds discount to the system
     *
     * @param discount the discount to add (must not be null)
     */
    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }


    /**
     * Adds a user to the system.
     *
     * @param user the user to add
     *
     */
    public void addUser(User user) {
        users.add(user);
    }


    /**
     * Adds a restaurant to the system.
     *
     * @param restaurant the restaurant to add
     */
    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
    }


    /**
     * Creates a new order for a user at a specific restaurant.
     *
     * @param id unique order identifier
     * @param userId ID of the customer
     * @param restaurant_id ID of the restaurant
     * @param type delivery method (IN_PERSON or COURIER)
     * @return a new Order instance, or null if restaurant not found
     */
    public Order createOrder(Integer id, Integer userId, Integer restaurant_id, Order.ReceivingType type) {
        return new Order(id, userId, restaurants.stream()
                .filter( r -> Objects.equals(r.getId(), restaurant_id) )
                .findFirst()
                .orElse(null), type);
    }

    public void setDate(LocalDate todayDate) {
        AdminPanel.todayDate = todayDate;
    }

    public Restaurant getBestRestaurant() {
        double maxScore = 0;
        Restaurant bestRestaurant = null;
        for (Restaurant r : restaurants) {
            if (r.getScore() > maxScore) {
                maxScore = r.getScore();
                bestRestaurant = r;
            }
        }
        return bestRestaurant;
    }


    /**
     * Finds the restaurant with the most orders
     *
     * @return the restaurant with highest order volume today, or null if no restaurants exist
     */
    public Restaurant getMostOrderedRestaurant() {
        int maxOrder = 0;
        Restaurant bestRestaurant = null;
        for (Restaurant r : restaurants) {
            int orderCount = r.getTodayOrdersCount();
            if (orderCount > maxOrder) {
                maxOrder = orderCount;
                bestRestaurant = r;
            }
        }
        return bestRestaurant;
    }
}
