package com.example.tripexpenseplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.example.tripexpenseplanner.model.Expense;
import com.example.tripexpenseplanner.model.ExpenseParticipant;
import com.example.tripexpenseplanner.model.Participant;
import com.example.tripexpenseplanner.model.Trip;
import com.example.tripexpenseplanner.model.TripActivity;

/**
 * Central SQLite access point for the whole app.
 * Creates the database schema and provides basic Insert / Update / Delete / Select
 * operations for every table. No UI logic lives here.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TripExpensePlanner.db";
    private static final int DATABASE_VERSION = 1;

    // ---------- trips ----------
    public static final String TABLE_TRIPS = "trips";
    public static final String COLUMN_TRIP_ID = "id";
    public static final String COLUMN_TRIP_NAME = "trip_name";
    public static final String COLUMN_TRIP_DESTINATION = "destination";
    public static final String COLUMN_TRIP_START_DATE = "start_date";
    public static final String COLUMN_TRIP_END_DATE = "end_date";
    public static final String COLUMN_TRIP_NOTES = "notes";

    // ---------- activities ----------
    public static final String TABLE_ACTIVITIES = "activities";
    public static final String COLUMN_ACTIVITY_ID = "id";
    public static final String COLUMN_ACTIVITY_TRIP_ID = "trip_id";
    public static final String COLUMN_ACTIVITY_NAME = "activity_name";
    public static final String COLUMN_ACTIVITY_DATE = "activity_date";
    public static final String COLUMN_ACTIVITY_TIME = "activity_time";
    public static final String COLUMN_ACTIVITY_DESCRIPTION = "description";

    // ---------- expenses ----------
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_TRIP_ID = "trip_id";
    public static final String COLUMN_EXPENSE_CATEGORY = "category";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_PAID_BY = "paid_by";
    public static final String COLUMN_EXPENSE_DESCRIPTION = "description";
    public static final String COLUMN_EXPENSE_DATE = "expense_date";

    // ---------- participants ----------
    public static final String TABLE_PARTICIPANTS = "participants";
    public static final String COLUMN_PARTICIPANT_ID = "id";
    public static final String COLUMN_PARTICIPANT_TRIP_ID = "trip_id";
    public static final String COLUMN_PARTICIPANT_NAME = "name";

    // ---------- expense_participants ----------
    public static final String TABLE_EXPENSE_PARTICIPANTS = "expense_participants";
    public static final String COLUMN_EP_ID = "id";
    public static final String COLUMN_EP_EXPENSE_ID = "expense_id";
    public static final String COLUMN_EP_PARTICIPANT_ID = "participant_id";
    public static final String COLUMN_EP_SHARE_AMOUNT = "share_amount";

    private static final String CREATE_TABLE_TRIPS =
            "CREATE TABLE " + TABLE_TRIPS + " (" +
                    COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRIP_NAME + " TEXT NOT NULL, " +
                    COLUMN_TRIP_DESTINATION + " TEXT NOT NULL, " +
                    COLUMN_TRIP_START_DATE + " TEXT NOT NULL, " +
                    COLUMN_TRIP_END_DATE + " TEXT NOT NULL, " +
                    COLUMN_TRIP_NOTES + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_ACTIVITIES =
            "CREATE TABLE " + TABLE_ACTIVITIES + " (" +
                    COLUMN_ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ACTIVITY_TRIP_ID + " INTEGER NOT NULL, " +
                    COLUMN_ACTIVITY_NAME + " TEXT NOT NULL, " +
                    COLUMN_ACTIVITY_DATE + " TEXT NOT NULL, " +
                    COLUMN_ACTIVITY_TIME + " TEXT, " +
                    COLUMN_ACTIVITY_DESCRIPTION + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_ACTIVITY_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ")" +
                    ");";

    private static final String CREATE_TABLE_EXPENSES =
            "CREATE TABLE " + TABLE_EXPENSES + " (" +
                    COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EXPENSE_TRIP_ID + " INTEGER NOT NULL, " +
                    COLUMN_EXPENSE_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                    COLUMN_EXPENSE_PAID_BY + " TEXT, " +
                    COLUMN_EXPENSE_DESCRIPTION + " TEXT, " +
                    COLUMN_EXPENSE_DATE + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_EXPENSE_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ")" +
                    ");";

    private static final String CREATE_TABLE_PARTICIPANTS =
            "CREATE TABLE " + TABLE_PARTICIPANTS + " (" +
                    COLUMN_PARTICIPANT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PARTICIPANT_TRIP_ID + " INTEGER NOT NULL, " +
                    COLUMN_PARTICIPANT_NAME + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_PARTICIPANT_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ")" +
                    ");";

    private static final String CREATE_TABLE_EXPENSE_PARTICIPANTS =
            "CREATE TABLE " + TABLE_EXPENSE_PARTICIPANTS + " (" +
                    COLUMN_EP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EP_EXPENSE_ID + " INTEGER NOT NULL, " +
                    COLUMN_EP_PARTICIPANT_ID + " INTEGER NOT NULL, " +
                    COLUMN_EP_SHARE_AMOUNT + " REAL NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_EP_EXPENSE_ID + ") REFERENCES " + TABLE_EXPENSES + "(" + COLUMN_EXPENSE_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_EP_PARTICIPANT_ID + ") REFERENCES " + TABLE_PARTICIPANTS + "(" + COLUMN_PARTICIPANT_ID + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enforce FOREIGN KEY constraints (off by default in SQLite).
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRIPS);
        db.execSQL(CREATE_TABLE_ACTIVITIES);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_PARTICIPANTS);
        db.execSQL(CREATE_TABLE_EXPENSE_PARTICIPANTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy for now: drop everything and recreate.
        // Child tables are dropped before their parent tables.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_PARTICIPANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTICIPANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }

    // =========================================================================================
    // TRIPS
    // =========================================================================================

    public long insertTrip(Trip trip) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_NAME, trip.getTripName());
        values.put(COLUMN_TRIP_DESTINATION, trip.getDestination());
        values.put(COLUMN_TRIP_START_DATE, trip.getStartDate());
        values.put(COLUMN_TRIP_END_DATE, trip.getEndDate());
        values.put(COLUMN_TRIP_NOTES, trip.getNotes());
        return db.insert(TABLE_TRIPS, null, values);
    }

    public int updateTrip(Trip trip) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_NAME, trip.getTripName());
        values.put(COLUMN_TRIP_DESTINATION, trip.getDestination());
        values.put(COLUMN_TRIP_START_DATE, trip.getStartDate());
        values.put(COLUMN_TRIP_END_DATE, trip.getEndDate());
        values.put(COLUMN_TRIP_NOTES, trip.getNotes());
        return db.update(TABLE_TRIPS, values, COLUMN_TRIP_ID + " = ?",
                new String[]{String.valueOf(trip.getId())});
    }

    public int deleteTrip(long tripId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRIPS, COLUMN_TRIP_ID + " = ?", new String[]{String.valueOf(tripId)});
    }

    public Trip getTrip(long tripId) {
        SQLiteDatabase db = getReadableDatabase();
        Trip trip = null;
        try (Cursor cursor = db.query(TABLE_TRIPS, null, COLUMN_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                trip = cursorToTrip(cursor);
            }
        }
        return trip;
    }

    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_TRIPS, null, null, null, null, null,
                COLUMN_TRIP_ID + " ASC")) {
            while (cursor.moveToNext()) {
                trips.add(cursorToTrip(cursor));
            }
        }
        return trips;
    }

    private Trip cursorToTrip(Cursor cursor) {
        Trip trip = new Trip();
        trip.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)));
        trip.setTripName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_NAME)));
        trip.setDestination(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_DESTINATION)));
        trip.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_START_DATE)));
        trip.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_END_DATE)));
        trip.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_NOTES)));
        return trip;
    }

    // =========================================================================================
    // ACTIVITIES
    // =========================================================================================

    public long insertActivity(TripActivity activity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTIVITY_TRIP_ID, activity.getTripId());
        values.put(COLUMN_ACTIVITY_NAME, activity.getActivityName());
        values.put(COLUMN_ACTIVITY_DATE, activity.getActivityDate());
        values.put(COLUMN_ACTIVITY_TIME, activity.getActivityTime());
        values.put(COLUMN_ACTIVITY_DESCRIPTION, activity.getDescription());
        return db.insert(TABLE_ACTIVITIES, null, values);
    }

    public int updateActivity(TripActivity activity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTIVITY_TRIP_ID, activity.getTripId());
        values.put(COLUMN_ACTIVITY_NAME, activity.getActivityName());
        values.put(COLUMN_ACTIVITY_DATE, activity.getActivityDate());
        values.put(COLUMN_ACTIVITY_TIME, activity.getActivityTime());
        values.put(COLUMN_ACTIVITY_DESCRIPTION, activity.getDescription());
        return db.update(TABLE_ACTIVITIES, values, COLUMN_ACTIVITY_ID + " = ?",
                new String[]{String.valueOf(activity.getId())});
    }

    public int deleteActivity(long activityId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ACTIVITIES, COLUMN_ACTIVITY_ID + " = ?", new String[]{String.valueOf(activityId)});
    }

    public TripActivity getActivity(long activityId) {
        SQLiteDatabase db = getReadableDatabase();
        TripActivity activity = null;
        try (Cursor cursor = db.query(TABLE_ACTIVITIES, null, COLUMN_ACTIVITY_ID + " = ?",
                new String[]{String.valueOf(activityId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                activity = cursorToActivity(cursor);
            }
        }
        return activity;
    }

    public List<TripActivity> getActivitiesByTrip(long tripId) {
        List<TripActivity> activities = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Order by date first, then time, so the itinerary reads chronologically.
        String orderBy = COLUMN_ACTIVITY_DATE + " ASC, " + COLUMN_ACTIVITY_TIME + " ASC";
        try (Cursor cursor = db.query(TABLE_ACTIVITIES, null, COLUMN_ACTIVITY_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)}, null, null, orderBy)) {
            while (cursor.moveToNext()) {
                activities.add(cursorToActivity(cursor));
            }
        }
        return activities;
    }

    private TripActivity cursorToActivity(Cursor cursor) {
        TripActivity activity = new TripActivity();
        activity.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_ID)));
        activity.setTripId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_TRIP_ID)));
        activity.setActivityName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_NAME)));
        activity.setActivityDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_DATE)));
        activity.setActivityTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_TIME)));
        activity.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_DESCRIPTION)));
        return activity;
    }

    // =========================================================================================
    // EXPENSES
    // =========================================================================================

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_TRIP_ID, expense.getTripId());
        values.put(COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_PAID_BY, expense.getPaidBy());
        values.put(COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(COLUMN_EXPENSE_DATE, expense.getExpenseDate());
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_TRIP_ID, expense.getTripId());
        values.put(COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_PAID_BY, expense.getPaidBy());
        values.put(COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(COLUMN_EXPENSE_DATE, expense.getExpenseDate());
        return db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expense.getId())});
    }

    public int deleteExpense(long expenseId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }

    public Expense getExpense(long expenseId) {
        SQLiteDatabase db = getReadableDatabase();
        Expense expense = null;
        try (Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expenseId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                expense = cursorToExpense(cursor);
            }
        }
        return expense;
    }

    public List<Expense> getExpensesByTrip(long tripId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXPENSE_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)}, null, null, COLUMN_EXPENSE_DATE + " ASC")) {
            while (cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();
        expense.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)));
        expense.setTripId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TRIP_ID)));
        expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CATEGORY)));
        expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT)));
        expense.setPaidBy(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_PAID_BY)));
        expense.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DESCRIPTION)));
        expense.setExpenseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE)));
        return expense;
    }

    /**
     * Sums the amount column across every expense for one trip, using SQLite's
     * own SUM() aggregate rather than adding the rows up in Java.
     * Returns 0 when the trip has no expenses yet (SUM() of no rows is NULL in SQL).
     */
    public double getTotalExpenseForTrip(long tripId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_EXPENSE_TRIP_ID + " = ?";
        double total = 0;
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tripId)})) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
        }
        return total;
    }

    // =========================================================================================
    // PARTICIPANTS
    // =========================================================================================

    public long insertParticipant(Participant participant) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_TRIP_ID, participant.getTripId());
        values.put(COLUMN_PARTICIPANT_NAME, participant.getName());
        return db.insert(TABLE_PARTICIPANTS, null, values);
    }

    public int updateParticipant(Participant participant) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_TRIP_ID, participant.getTripId());
        values.put(COLUMN_PARTICIPANT_NAME, participant.getName());
        return db.update(TABLE_PARTICIPANTS, values, COLUMN_PARTICIPANT_ID + " = ?",
                new String[]{String.valueOf(participant.getId())});
    }

    public int deleteParticipant(long participantId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PARTICIPANTS, COLUMN_PARTICIPANT_ID + " = ?",
                new String[]{String.valueOf(participantId)});
    }

    public Participant getParticipant(long participantId) {
        SQLiteDatabase db = getReadableDatabase();
        Participant participant = null;
        try (Cursor cursor = db.query(TABLE_PARTICIPANTS, null, COLUMN_PARTICIPANT_ID + " = ?",
                new String[]{String.valueOf(participantId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                participant = cursorToParticipant(cursor);
            }
        }
        return participant;
    }

    public List<Participant> getParticipantsByTrip(long tripId) {
        List<Participant> participants = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_PARTICIPANTS, null, COLUMN_PARTICIPANT_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)}, null, null, COLUMN_PARTICIPANT_NAME + " ASC")) {
            while (cursor.moveToNext()) {
                participants.add(cursorToParticipant(cursor));
            }
        }
        return participants;
    }

    private Participant cursorToParticipant(Cursor cursor) {
        Participant participant = new Participant();
        participant.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_ID)));
        participant.setTripId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_TRIP_ID)));
        participant.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_NAME)));
        return participant;
    }

    // =========================================================================================
    // EXPENSE_PARTICIPANTS
    // =========================================================================================

    public long insertExpenseParticipant(ExpenseParticipant expenseParticipant) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EP_EXPENSE_ID, expenseParticipant.getExpenseId());
        values.put(COLUMN_EP_PARTICIPANT_ID, expenseParticipant.getParticipantId());
        values.put(COLUMN_EP_SHARE_AMOUNT, expenseParticipant.getShareAmount());
        return db.insert(TABLE_EXPENSE_PARTICIPANTS, null, values);
    }

    public int updateExpenseParticipant(ExpenseParticipant expenseParticipant) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EP_EXPENSE_ID, expenseParticipant.getExpenseId());
        values.put(COLUMN_EP_PARTICIPANT_ID, expenseParticipant.getParticipantId());
        values.put(COLUMN_EP_SHARE_AMOUNT, expenseParticipant.getShareAmount());
        return db.update(TABLE_EXPENSE_PARTICIPANTS, values, COLUMN_EP_ID + " = ?",
                new String[]{String.valueOf(expenseParticipant.getId())});
    }

    public int deleteExpenseParticipant(long expenseParticipantId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EXPENSE_PARTICIPANTS, COLUMN_EP_ID + " = ?",
                new String[]{String.valueOf(expenseParticipantId)});
    }

    /**
     * Deletes every split (expense_participants row) recorded for one expense.
     * Used before re-saving an edited expense's splits from scratch, and before
     * deleting the expense itself — the "expense_id" foreign key means an expense
     * with splits still pointing at it cannot be deleted otherwise.
     */
    public int deleteExpenseParticipantsByExpense(long expenseId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EXPENSE_PARTICIPANTS, COLUMN_EP_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expenseId)});
    }

    /**
     * Deletes every split (expense_participants row) that involves one participant.
     * Used before deleting the participant itself — the "participant_id" foreign
     * key means a participant who is still part of a split cannot be deleted otherwise.
     * Note: this simply removes that participant's share from any expense they were
     * part of; it does not redistribute the amount among the remaining participants.
     */
    public int deleteExpenseParticipantsByParticipant(long participantId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EXPENSE_PARTICIPANTS, COLUMN_EP_PARTICIPANT_ID + " = ?",
                new String[]{String.valueOf(participantId)});
    }

    public List<ExpenseParticipant> getExpenseParticipantsByExpense(long expenseId) {
        List<ExpenseParticipant> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_EXPENSE_PARTICIPANTS, null, COLUMN_EP_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expenseId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                list.add(cursorToExpenseParticipant(cursor));
            }
        }
        return list;
    }

    private ExpenseParticipant cursorToExpenseParticipant(Cursor cursor) {
        ExpenseParticipant expenseParticipant = new ExpenseParticipant();
        expenseParticipant.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EP_ID)));
        expenseParticipant.setExpenseId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EP_EXPENSE_ID)));
        expenseParticipant.setParticipantId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EP_PARTICIPANT_ID)));
        expenseParticipant.setShareAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EP_SHARE_AMOUNT)));
        return expenseParticipant;
    }
}
