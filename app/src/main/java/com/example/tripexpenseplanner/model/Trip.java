package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "trips" table.
 */
public class Trip {

    private long id;
    private String tripName;
    private String destination;
    private String startDate;
    private String endDate;
    private String notes;

    public Trip() {
    }

    public Trip(String tripName, String destination, String startDate, String endDate, String notes) {
        this.tripName = tripName;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
