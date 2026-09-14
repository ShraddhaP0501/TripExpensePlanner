package com.example.tripexpenseplanner.model;

/**
 * Represents a single row in the "reminders" table.
 * reminderDate is stored as "yyyy-MM-dd" and reminderTime as 24-hour "HH:mm"
 * (not the 12-hour "hh:mm a" used elsewhere) so the two can be combined into
 * an exact, unambiguous trigger time for the Android alarm that backs it.
 */
public class Reminder {

    private long id;
    private long tripId;
    private String title;
    private String reminderType;
    private String reminderDate;
    private String reminderTime;
    private String note;

    public Reminder() {
    }

    public Reminder(long tripId, String title, String reminderType, String reminderDate,
                     String reminderTime, String note) {
        this.tripId = tripId;
        this.title = title;
        this.reminderType = reminderType;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.note = note;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
