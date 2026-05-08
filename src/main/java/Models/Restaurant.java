package Models;

import JavaFood.AdminPanel;
import lombok.Data;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Restaurant {
    public enum RestaurantTypes {
        FASTFOOD,
        IRANI,
        VEGETARIAN,
        KEBAB,
        SALAD,
        CAFE,
        SUPERMARKET,
        COFFEE
    }

    private RestaurantTypes type;
    private Integer id;
    private String name;
    private Double score = 0.0;
    private Integer scoreCounts = 0;
    private String address;
    int openHour, closeHour;
    // Food - quantity
    final LinkedHashMap<Food, Integer> foods = new LinkedHashMap<>();
    private Boolean isOpen = false;

    public Restaurant(Integer id, String name, String address, int openHour, int closeHour, RestaurantTypes type) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.openHour = openHour;
        this.closeHour = closeHour;
        this.type = type;
    }

    public void openRestaurant() {
        isOpen = true;
    }

    public void closeRestaurant() {
        isOpen = false;
    }

    public void addFood(Food food, Integer quantity) {
        foods.put(food, quantity);
    }

    public int getTodayOrdersCount(){
        return (int) AdminPanel.orders.stream().filter(o ->
                o.getOrderDateTime().toLocalDate().equals(AdminPanel.todayDate) && o.getRestaurant().id.equals(this.id)
        ).count();
    }

    public Double getTodayOrdersAmount(){
        return AdminPanel.orders.stream()
                .filter(o -> o.getOrderDateTime().toLocalDate().equals(AdminPanel.todayDate) && o.getRestaurant().id.equals(this.id))
                .mapToDouble(Order::getTotalPrice).sum();
    }

    public void updateFoodQuantity(Food food, Integer quantity) {
        foods.put(food, quantity);
    }

    public Food getMostOrderedFood (){
        HashMap<Food, Integer> orderedFoods = new LinkedHashMap<>();
        AdminPanel.orders.stream().filter(o ->
                o.getRestaurant().id.equals(this.id)).forEach(o -> {
                    o.getFoods().keySet().forEach(f -> {
                        if (orderedFoods.containsKey(f)) {
                            orderedFoods.put(f, orderedFoods.get(f) + o.getFoods().get(f));
                        }
                        else {
                            orderedFoods.put(f, o.getFoods().get(f));
                        }
                    });
                }
        );
        
        int max = 0;
        Food food = null;
        for (Map.Entry<Food, Integer> entry : orderedFoods.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                food = entry.getKey();
            }
        }
        return food;
    }
}
