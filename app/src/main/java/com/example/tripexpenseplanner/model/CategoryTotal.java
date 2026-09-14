package com.example.tripexpenseplanner.model;

/**
 * A calculated (not stored) row for the Trip Summary screen: the total amount
 * spent in one expense category (e.g. "Food") for a trip. Built from a
 * GROUP BY query over the "expenses" table — not persisted on its own.
 */
public class CategoryTotal {

    private final String category;
    private final double totalAmount;

    public CategoryTotal(String category, double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public String getCategory() {
        return category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
