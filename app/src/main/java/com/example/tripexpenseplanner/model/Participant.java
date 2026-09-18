package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "participants" table.
 */
public class Participant {

    private long id;
    private long tripId;
    private String name;
    private String contact;
    private long userId;

    public Participant() {
    }

    public Participant(long tripId, String name) {
        this.tripId = tripId;
        this.name = name;
    }

    public Participant(long tripId, String name, String contact, long userId) {
        this.tripId = tripId;
        this.name = name;
        this.contact = contact;
        this.userId = userId;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
