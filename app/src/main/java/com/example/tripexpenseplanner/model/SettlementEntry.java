package com.example.tripexpenseplanner.model;

/**
 * One line of the "who owes whom" settlement list on the Trip Summary screen:
 * {@code fromName} owes {@code toName} the given {@code amount}.
 * Calculated on the fly from every participant's overall balance — not stored.
 */
public class SettlementEntry {

    private final String fromName;
    private final String toName;
    private final double amount;
    private final long fromMemberId;
    private final long toMemberId;

    public SettlementEntry(String fromName, String toName, double amount) {
        this(0, 0, fromName, toName, amount);
    }

    public SettlementEntry(long fromMemberId, long toMemberId, String fromName, String toName, double amount) {
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.fromName = fromName;
        this.toName = toName;
        this.amount = amount;
    }

    public String getFromName() {
        return fromName;
    }

    public String getToName() {
        return toName;
    }

    public double getAmount() {
        return amount;
    }

    public long getFromMemberId() { return fromMemberId; }

    public long getToMemberId() { return toMemberId; }
}
