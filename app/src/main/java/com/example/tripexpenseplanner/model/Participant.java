package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "participants" table.
 */
public class Participant {

    private long id;
    private long tripId;
    private String name;

    public Participant() {
    }

    public Participant(long tripId, String name) {
        this.tripId = tripId;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
