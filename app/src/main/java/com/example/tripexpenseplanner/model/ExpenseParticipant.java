package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "expense_participants" table.
 * Links an expense to a participant and records that participant's share of the amount.
 */
public class ExpenseParticipant {

    private long id;
    private long expenseId;
    private long participantId;
    private double shareAmount;

    public ExpenseParticipant() {
    }

    public ExpenseParticipant(long expenseId, long participantId, double shareAmount) {
        this.expenseId = expenseId;
        this.participantId = participantId;
        this.shareAmount = shareAmount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(long expenseId) {
        this.expenseId = expenseId;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    public double getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(double shareAmount) {
        this.shareAmount = shareAmount;
    }
}
