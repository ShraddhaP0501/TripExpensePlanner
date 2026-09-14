package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "expenses" table.
 */
public class Expense {

    private long id;
    private long tripId;
    private String category;
    private double amount;
    private String paidBy;
    private String description;
    private String expenseDate;

    public Expense() {
    }

    public Expense(long tripId, String category, double amount, String paidBy, String description, String expenseDate) {
        this.tripId = tripId;
        this.category = category;
        this.amount = amount;
        this.paidBy = paidBy;
        this.description = description;
        this.expenseDate = expenseDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }
}
