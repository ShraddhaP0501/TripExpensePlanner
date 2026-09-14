package com.example.tripexpenseplanner.model;

/**
 * A calculated (not stored) row for the Trip Summary screen: how much one
 * participant paid across every expense in the trip, versus their total share
 * of those expenses. Not persisted to SQLite — built on the fly from
 * expenses + expense_participants each time the summary is shown.
 */
public class ParticipantBalance {

    private final long participantId;
    private final String participantName;
    private final double totalPaid;
    private final double totalShare;

    public ParticipantBalance(long participantId, String participantName, double totalPaid, double totalShare) {
        this.participantId = participantId;
        this.participantName = participantName;
        this.totalPaid = totalPaid;
        this.totalShare = totalShare;
    }

    public long getParticipantId() {
        return participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public double getTotalShare() {
        return totalShare;
    }

    /**
     * Positive: this participant paid more than their share (the group owes them).
     * Negative: this participant paid less than their share (they owe the group).
     */
    public double getBalance() {
        return totalPaid - totalShare;
    }
}
