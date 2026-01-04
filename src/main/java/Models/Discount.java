package Models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Discount {
    private int id;
    private String code;
    private Integer userId;
    private DiscountType discountType;
    private LocalDate expireDate;
    private  Double amount;
    private Integer percentage;
    private Boolean isUsed = false;

    public enum DiscountType{
        PERCENTAGE,
        AMOUNT
    }

    public Discount(Integer id, String code, Double amount, LocalDate expireDate, Integer userId) {
        this.id = id;
        this.code = code;
        this.amount = amount;
        this.expireDate = expireDate;
        this.userId = userId;
    }

    public Discount(Integer id, String code, Integer percentage, LocalDate expireDate, Integer userId) {
        this.id = id;
        this.code = code;
        this.percentage = percentage;
        this.expireDate = expireDate;
        this.userId = userId;
    }
}