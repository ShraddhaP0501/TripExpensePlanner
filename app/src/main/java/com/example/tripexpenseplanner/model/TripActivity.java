package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "activities" table.
 * Named "TripActivity" (not "Activity") to avoid clashing with android.app.Activity.
 */
public class TripActivity {

    private long id;
    private long tripId;
    private String activityName;
    private String activityDate;
    private String activityTime;
    private String description;

    public TripActivity() {
    }

    public TripActivity(long tripId, String activityName, String activityDate, String activityTime, String description) {
        this.tripId = tripId;
        this.activityName = activityName;
        this.activityDate = activityDate;
        this.activityTime = activityTime;
        this.description = description;
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

    public String getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(String activityTime) {
        this.activityTime = activityTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
