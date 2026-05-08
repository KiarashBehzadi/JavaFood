package Models;

import Exceptions.*;
import JavaFood.AdminPanel;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Data
public class Order {
    private Integer id;
    private Double discountPrice = 0.0;
    private Integer userId;
    private Restaurant restaurant;
    private LocalDateTime orderDateTime;
    private Double totalPrice = 0.0;
    private ReceivingType receivingType;
    private Boolean isPaid = false, isScored = false;
    private Integer score;
    private final HashMap<Food, Integer> foods = new HashMap<>();

    public Order(Integer id, Integer userId, Restaurant restaurant, ReceivingType receivingType) {
        this.id = id;
        this.userId = userId;
        this.restaurant = restaurant;
        this.receivingType = receivingType;
    }

    public enum ReceivingType {
        IN_PERSON,
        COURIER
    }
    
    /**
     * Adds a food to the current order
     * This method validates:
     *  - Food exists in restaurant menu
     *  - Sufficient quantity available
     *  - Restaurant is open for ordering
     * @throws FoodNotFound   if foodId doesn't exist in Restaurant
     * @throws FoodOutOfStock  if requested quantity exceeds available stock
     * @throws RestaurantIsClose  if Restaurant is not open
     */
    public void addFoodToOrder(Integer foodId, int orderQuantity) {
        Food food = restaurant.foods.keySet().stream()
                .filter( f -> f.getId().equals(foodId) )
                .findFirst().orElse(null);
        if (food == null) {
            throw new FoodNotFound();
        }
        Integer foodQuantity = restaurant.foods.get(food);
        if (orderQuantity > foodQuantity) {
            throw new FoodOutOfStock();
        }
        if (!restaurant.getIsOpen()) {
            throw new RestaurantIsClose();
        }
        foods.put(food, orderQuantity);
        restaurant.updateFoodQuantity(food, foodQuantity - orderQuantity);
        totalPrice += (food.getPrice() * orderQuantity);
    }

    /**
     * Applies a discount code to this order.
     * validates discount code exists, is unused, not expired, and belongs to the user.
     * @param code  the discount code
     * @throws InvalidDiscountCode   if the discount code is invalid, expired, used, or not belongs to user
     */
    public void addDiscount(String code) {
        Discount discount = AdminPanel.discounts.stream()
                .filter(d -> d.getCode().equals(code)).
                findFirst()
                .orElse(null);
        if (discount == null) {
            throw new InvalidDiscountCode("Discount code not found");
        }
        else if (discount.getIsUsed()){
            throw new InvalidDiscountCode("Discount code already used");
        }
        else if (discount.getExpireDate().isBefore(AdminPanel.todayDate)) {
            throw new InvalidDiscountCode("Discount is over");
        }
        else if (!discount.getUserId().equals(userId)) {
            throw new InvalidDiscountCode("This discount is not for you");
        }

        if (discount.getDiscountType().equals(Discount.DiscountType.AMOUNT)) {
            totalPrice -= discount.getAmount();
            discountPrice += discount.getAmount();
        } else if (discount.getDiscountType().equals(Discount.DiscountType.PERCENTAGE)) {
            double dAmount = totalPrice * discount.getPercentage() / 100;
            totalPrice -= dAmount;
            discountPrice += dAmount;
        }
        discount.setIsUsed(true);
    }


    /**
     * Process payment for the order.
     * @param amount  the amount paid
     * @throws PayException   if the amount doesn't match totalPrice
     */
    public void pay(Double amount) {
        if (!totalPrice.equals(amount)) {
            throw new PayException();
        }

        // Format: Year/Month/Day Hour:Minute (e.g., 2025/01/01 14:30)
        orderDateTime = LocalDateTime.of(AdminPanel.todayDate,  LocalTime.now());
        orderDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        isPaid = true;
        AdminPanel.orders.add(this);
    }
    
    
    /**
     * Submits a customer rating for the Restaurant after payment
     * @param score  score rating (from 1=worst to 5=best)
     * @throws InvalidScore   if already scored, order not paid yet, or score out of range
     */
    public void scoreOrder (Integer score) {
        if (isScored) {
            throw new InvalidScore("Already scored");
        }
        if (!isPaid){
            throw new InvalidScore("Not paid yet");
        }
        if (score < 1 || score > 5) {
            throw new InvalidScore("Invalid score");
        }
        this.score = score;
        isScored = true;
        Double avg = ((restaurant.getScore() * restaurant.getScoreCounts()) + score) / (restaurant.getScoreCounts() == 0 ? 1 : restaurant.getScoreCounts() + 1);
        restaurant.setScoreCounts(restaurant.getScoreCounts() + 1);
        restaurant.setScore(Double.parseDouble(String.format("%.1f", avg)));
    }
}
