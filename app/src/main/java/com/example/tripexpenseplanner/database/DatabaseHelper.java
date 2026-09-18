package com.example.tripexpenseplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.example.tripexpenseplanner.model.CategoryTotal;
import com.example.tripexpenseplanner.model.Expense;
import com.example.tripexpenseplanner.model.ExpenseParticipant;
import com.example.tripexpenseplanner.model.Participant;
import com.example.tripexpenseplanner.model.Reminder;
import com.example.tripexpenseplanner.model.Trip;
import com.example.tripexpenseplanner.model.TripActivity;
import com.example.tripexpenseplanner.auth.AuthSession;

/**
 * Central SQLite access point for the whole app.
 * Creates the database schema and provides basic Insert / Update / Delete / Select
 * operations for every table. No UI logic lives here.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TripExpensePlanner.db";
    private static final int DATABASE_VERSION = 4;
    private final Context context;

    // ---------- users ----------
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_USER_CREATED_AT = "created_at";

    // ---------- trips ----------
    public static final String TABLE_TRIPS = "trips";
    public static final String COLUMN_TRIP_ID = "id";
    public static final String COLUMN_TRIP_NAME = "trip_name";
    public static final String COLUMN_TRIP_DESTINATION = "destination";
    public static final String COLUMN_TRIP_START_DATE = "start_date";
    public static final String COLUMN_TRIP_END_DATE = "end_date";
    public static final String COLUMN_TRIP_NOTES = "notes";
    public static final String COLUMN_TRIP_USER_ID = "user_id";

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
    public static final String COLUMN_EXPENSE_PAID_BY_MEMBER_ID = "paid_by_member_id";

    // ---------- participants ----------
    public static final String TABLE_PARTICIPANTS = "participants";
    public static final String COLUMN_PARTICIPANT_ID = "id";
    public static final String COLUMN_PARTICIPANT_TRIP_ID = "trip_id";
    public static final String COLUMN_PARTICIPANT_NAME = "name";
    public static final String COLUMN_PARTICIPANT_CONTACT = "contact";
    public static final String COLUMN_PARTICIPANT_USER_ID = "user_id";

    // ---------- settlements ----------
    public static final String TABLE_SETTLEMENTS = "settlements";
    public static final String COLUMN_SETTLEMENT_ID = "id";
    public static final String COLUMN_SETTLEMENT_TRIP_ID = "trip_id";
    public static final String COLUMN_SETTLEMENT_FROM_MEMBER_ID = "from_member_id";
    public static final String COLUMN_SETTLEMENT_TO_MEMBER_ID = "to_member_id";
    public static final String COLUMN_SETTLEMENT_AMOUNT = "amount";
    public static final String COLUMN_SETTLEMENT_DATE = "date";
    public static final String COLUMN_SETTLEMENT_STATUS = "status";

    // ---------- expense_participants ----------
    public static final String TABLE_EXPENSE_PARTICIPANTS = "expense_participants";
    public static final String COLUMN_EP_ID = "id";
    public static final String COLUMN_EP_EXPENSE_ID = "expense_id";
    public static final String COLUMN_EP_PARTICIPANT_ID = "participant_id";
    public static final String COLUMN_EP_SHARE_AMOUNT = "share_amount";

    // ---------- reminders ----------
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COLUMN_REMINDER_ID = "id";
    public static final String COLUMN_REMINDER_TRIP_ID = "trip_id";
    public static final String COLUMN_REMINDER_TITLE = "title";
    public static final String COLUMN_REMINDER_TYPE = "reminder_type";
    public static final String COLUMN_REMINDER_DATE = "reminder_date";
    public static final String COLUMN_REMINDER_TIME = "reminder_time";
    public static final String COLUMN_REMINDER_NOTE = "note";

    private static final String CREATE_TABLE_TRIPS =
            "CREATE TABLE " + TABLE_TRIPS + " (" +
                    COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRIP_NAME + " TEXT NOT NULL, " +
                    COLUMN_TRIP_DESTINATION + " TEXT NOT NULL, " +
                    COLUMN_TRIP_START_DATE + " TEXT NOT NULL, " +
                    COLUMN_TRIP_END_DATE + " TEXT NOT NULL, " +
                        COLUMN_TRIP_NOTES + " TEXT, " +
                        COLUMN_TRIP_USER_ID + " INTEGER" +
                    ");";

                private static final String CREATE_TABLE_USERS =
                    "CREATE TABLE " + TABLE_USERS + " (" +
                        COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_NAME + " TEXT NOT NULL, " +
                        COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
                        COLUMN_USER_CREATED_AT + " INTEGER NOT NULL" +
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
                    COLUMN_EXPENSE_PAID_BY_MEMBER_ID + " INTEGER, " +
                    COLUMN_EXPENSE_DESCRIPTION + " TEXT, " +
                    COLUMN_EXPENSE_DATE + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_EXPENSE_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ")" +
                    ");";

    private static final String CREATE_TABLE_PARTICIPANTS =
            "CREATE TABLE " + TABLE_PARTICIPANTS + " (" +
                    COLUMN_PARTICIPANT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PARTICIPANT_TRIP_ID + " INTEGER NOT NULL, " +
                    COLUMN_PARTICIPANT_NAME + " TEXT NOT NULL, " +
                        COLUMN_PARTICIPANT_CONTACT + " TEXT, " +
                        COLUMN_PARTICIPANT_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_PARTICIPANT_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ")" +
                    ");";

                private static final String CREATE_TABLE_SETTLEMENTS =
                    "CREATE TABLE " + TABLE_SETTLEMENTS + " (" +
                        COLUMN_SETTLEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_SETTLEMENT_TRIP_ID + " INTEGER NOT NULL, " +
                        COLUMN_SETTLEMENT_FROM_MEMBER_ID + " INTEGER NOT NULL, " +
                        COLUMN_SETTLEMENT_TO_MEMBER_ID + " INTEGER NOT NULL, " +
                        COLUMN_SETTLEMENT_AMOUNT + " REAL NOT NULL, " +
                        COLUMN_SETTLEMENT_DATE + " TEXT NOT NULL, " +
                        COLUMN_SETTLEMENT_STATUS + " TEXT NOT NULL, " +
                        "FOREIGN KEY(" + COLUMN_SETTLEMENT_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + "), " +
                        "FOREIGN KEY(" + COLUMN_SETTLEMENT_FROM_MEMBER_ID + ") REFERENCES " + TABLE_PARTICIPANTS + "(" + COLUMN_PARTICIPANT_ID + "), " +
                        "FOREIGN KEY(" + COLUMN_SETTLEMENT_TO_MEMBER_ID + ") REFERENCES " + TABLE_PARTICIPANTS + "(" + COLUMN_PARTICIPANT_ID + ")" +
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

    private static final String CREATE_TABLE_REMINDERS =
            "CREATE TABLE " + TABLE_REMINDERS + " (" +
                    COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_REMINDER_TRIP_ID + " INTEGER NOT NULL, " +
                    COLUMN_REMINDER_TITLE + " TEXT NOT NULL, " +
                    COLUMN_REMINDER_TYPE + " TEXT NOT NULL, " +
                    COLUMN_REMINDER_DATE + " TEXT NOT NULL, " +
                    COLUMN_REMINDER_TIME + " TEXT NOT NULL, " +
                    COLUMN_REMINDER_NOTE + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_REMINDER_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enforce FOREIGN KEY constraints (off by default in SQLite).
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TRIPS);
        db.execSQL(CREATE_TABLE_ACTIVITIES);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_PARTICIPANTS);
        db.execSQL(CREATE_TABLE_EXPENSE_PARTICIPANTS);
        db.execSQL(CREATE_TABLE_REMINDERS);
        db.execSQL(CREATE_TABLE_SETTLEMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL("ALTER TABLE " + TABLE_TRIPS + " ADD COLUMN " + COLUMN_TRIP_USER_ID + " INTEGER");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_EXPENSE_PAID_BY_MEMBER_ID + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PARTICIPANTS + " ADD COLUMN " + COLUMN_PARTICIPANT_CONTACT + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PARTICIPANTS + " ADD COLUMN " + COLUMN_PARTICIPANT_USER_ID + " INTEGER");
            db.execSQL(CREATE_TABLE_SETTLEMENTS);
        }
    }

    public long createUser(String name, String email, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD_HASH, passwordHash);
        values.put(COLUMN_USER_CREATED_AT, System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE_USERS, null, values);
    }

    public Cursor findUserByEmail(String email) {
        return getReadableDatabase().query(TABLE_USERS, null, COLUMN_USER_EMAIL + " = ?",
                new String[]{email}, null, null, null);
    }

    public Cursor findUserById(long userId) {
        return getReadableDatabase().query(TABLE_USERS, null, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);
    }

    public int updateUserPassword(long userId, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PASSWORD_HASH, passwordHash);
        return getWritableDatabase().update(TABLE_USERS, values, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
    }

    public String getUserName(long userId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_USERS,
                new String[]{COLUMN_USER_NAME}, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    public void claimUnownedTrips(long userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_USER_ID, userId);
        getWritableDatabase().update(TABLE_TRIPS, values, COLUMN_TRIP_USER_ID + " IS NULL", null);
    }

    private long currentUserId() {
        return AuthSession.getUserId(context);
    }

    // =========================================================================================
    // TRIPS
    // =========================================================================================

    public long insertTrip(Trip trip) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_USER_ID, currentUserId());
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
        return db.update(TABLE_TRIPS, values, COLUMN_TRIP_ID + " = ? AND " + COLUMN_TRIP_USER_ID + " = ?",
            new String[]{String.valueOf(trip.getId()), String.valueOf(currentUserId())});
    }

    public int deleteTrip(long tripId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRIPS, COLUMN_TRIP_ID + " = ? AND " + COLUMN_TRIP_USER_ID + " = ?",
            new String[]{String.valueOf(tripId), String.valueOf(currentUserId())});
    }

    public Trip getTrip(long tripId) {
        SQLiteDatabase db = getReadableDatabase();
        Trip trip = null;
        try (Cursor cursor = db.query(TABLE_TRIPS, null, COLUMN_TRIP_ID + " = ? AND " + COLUMN_TRIP_USER_ID + " = ?",
            new String[]{String.valueOf(tripId), String.valueOf(currentUserId())}, null, null, null)) {
            if (cursor.moveToFirst()) {
                trip = cursorToTrip(cursor);
            }
        }
        return trip;
    }

    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_TRIPS, null, COLUMN_TRIP_USER_ID + " = ?",
            new String[]{String.valueOf(currentUserId())}, null, null,
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
        String selection = COLUMN_ACTIVITY_TRIP_ID + " = ? AND EXISTS (SELECT 1 FROM " + TABLE_TRIPS
            + " WHERE " + TABLE_TRIPS + "." + COLUMN_TRIP_ID + " = " + TABLE_ACTIVITIES + "."
            + COLUMN_ACTIVITY_TRIP_ID + " AND " + COLUMN_TRIP_USER_ID + " = ?)";
        try (Cursor cursor = db.query(TABLE_ACTIVITIES, null, selection,
            new String[]{String.valueOf(tripId), String.valueOf(currentUserId())}, null, null, orderBy)) {
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
        return insertExpense(expense, null);
    }

    public long insertExpense(Expense expense, Long paidByMemberId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_TRIP_ID, expense.getTripId());
        values.put(COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_PAID_BY, expense.getPaidBy());
        if (paidByMemberId != null) {
            values.put(COLUMN_EXPENSE_PAID_BY_MEMBER_ID, paidByMemberId);
        }
        values.put(COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(COLUMN_EXPENSE_DATE, expense.getExpenseDate());
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public int updateExpense(Expense expense) {
        return updateExpense(expense, null);
    }

    public int updateExpense(Expense expense, Long paidByMemberId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_TRIP_ID, expense.getTripId());
        values.put(COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_PAID_BY, expense.getPaidBy());
        if (paidByMemberId != null) {
            values.put(COLUMN_EXPENSE_PAID_BY_MEMBER_ID, paidByMemberId);
        }
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

    public long getExpensePaidByMemberId(long expenseId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_EXPENSES,
                new String[]{COLUMN_EXPENSE_PAID_BY_MEMBER_ID}, COLUMN_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expenseId)}, null, null, null)) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) return cursor.getLong(0);
        }
        return -1L;
    }

    public List<Expense> getExpensesByTrip(long tripId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_EXPENSE_TRIP_ID + " = ? AND EXISTS (SELECT 1 FROM " + TABLE_TRIPS
            + " WHERE " + TABLE_TRIPS + "." + COLUMN_TRIP_ID + " = " + TABLE_EXPENSES + "."
            + COLUMN_EXPENSE_TRIP_ID + " AND " + COLUMN_TRIP_USER_ID + " = ?)";
        try (Cursor cursor = db.query(TABLE_EXPENSES, null, selection,
            new String[]{String.valueOf(tripId), String.valueOf(currentUserId())}, null, null, COLUMN_EXPENSE_DATE + " ASC")) {
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

    /**
     * Groups every expense for one trip by its category and sums each group's amount,
     * using SQLite's own GROUP BY + SUM() rather than grouping the rows in Java.
     * Categories with no expenses are simply absent from the result.
     */
    public List<CategoryTotal> getCategoryWiseExpenseTotals(long tripId) {
        List<CategoryTotal> totals = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_EXPENSE_CATEGORY + ", SUM(" + COLUMN_EXPENSE_AMOUNT + ") " +
                "FROM " + TABLE_EXPENSES +
                " WHERE " + COLUMN_EXPENSE_TRIP_ID + " = ?" +
                " GROUP BY " + COLUMN_EXPENSE_CATEGORY +
                " ORDER BY SUM(" + COLUMN_EXPENSE_AMOUNT + ") DESC";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tripId)})) {
            while (cursor.moveToNext()) {
                String category = cursor.getString(0);
                double total = cursor.getDouble(1);
                totals.add(new CategoryTotal(category, total));
            }
        }
        return totals;
    }

    // =========================================================================================
    // PARTICIPANTS
    // =========================================================================================

    public long insertParticipant(Participant participant) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_TRIP_ID, participant.getTripId());
        values.put(COLUMN_PARTICIPANT_NAME, participant.getName());
        values.put(COLUMN_PARTICIPANT_CONTACT, participant.getContact());
        if (participant.getUserId() > 0) values.put(COLUMN_PARTICIPANT_USER_ID, participant.getUserId());
        return db.insert(TABLE_PARTICIPANTS, null, values);
    }

    public long ensureCurrentUserParticipant(long tripId) {
        long userId = currentUserId();
        try (Cursor cursor = getReadableDatabase().query(TABLE_PARTICIPANTS,
                new String[]{COLUMN_PARTICIPANT_ID}, COLUMN_PARTICIPANT_TRIP_ID + " = ? AND " + COLUMN_PARTICIPANT_USER_ID + " = ?",
                new String[]{String.valueOf(tripId), String.valueOf(userId)}, null, null, null)) {
            if (cursor.moveToFirst()) return cursor.getLong(0);
        }
        return insertParticipant(new Participant(tripId, AuthSession.getUserName(context), null, userId));
    }

    public long getCurrentUserParticipantId(long tripId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_PARTICIPANTS,
                new String[]{COLUMN_PARTICIPANT_ID}, COLUMN_PARTICIPANT_TRIP_ID + " = ? AND " + COLUMN_PARTICIPANT_USER_ID + " = ?",
                new String[]{String.valueOf(tripId), String.valueOf(currentUserId())}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1L;
        }
    }

    public boolean participantNameExists(long tripId, String name, long excludedId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_PARTICIPANTS,
                new String[]{COLUMN_PARTICIPANT_ID}, COLUMN_PARTICIPANT_TRIP_ID + " = ? AND lower(" + COLUMN_PARTICIPANT_NAME + ") = lower(?) AND " + COLUMN_PARTICIPANT_ID + " != ?",
                new String[]{String.valueOf(tripId), name, String.valueOf(excludedId)}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public int updateParticipant(Participant participant) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_TRIP_ID, participant.getTripId());
        values.put(COLUMN_PARTICIPANT_NAME, participant.getName());
        values.put(COLUMN_PARTICIPANT_CONTACT, participant.getContact());
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
        String selection = COLUMN_PARTICIPANT_TRIP_ID + " = ? AND EXISTS (SELECT 1 FROM " + TABLE_TRIPS
            + " WHERE " + TABLE_TRIPS + "." + COLUMN_TRIP_ID + " = " + TABLE_PARTICIPANTS + "."
            + COLUMN_PARTICIPANT_TRIP_ID + " AND " + COLUMN_TRIP_USER_ID + " = ?)";
        try (Cursor cursor = db.query(TABLE_PARTICIPANTS, null, selection,
            new String[]{String.valueOf(tripId), String.valueOf(currentUserId())}, null, null, COLUMN_PARTICIPANT_NAME + " ASC")) {
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
        int contactIndex = cursor.getColumnIndex(COLUMN_PARTICIPANT_CONTACT);
        if (contactIndex >= 0) participant.setContact(cursor.getString(contactIndex));
        int userIndex = cursor.getColumnIndex(COLUMN_PARTICIPANT_USER_ID);
        if (userIndex >= 0 && !cursor.isNull(userIndex)) participant.setUserId(cursor.getLong(userIndex));
        return participant;
    }

    public long insertSettlement(long tripId, long fromMemberId, long toMemberId, double amount) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SETTLEMENT_TRIP_ID, tripId);
        values.put(COLUMN_SETTLEMENT_FROM_MEMBER_ID, fromMemberId);
        values.put(COLUMN_SETTLEMENT_TO_MEMBER_ID, toMemberId);
        values.put(COLUMN_SETTLEMENT_AMOUNT, amount);
        values.put(COLUMN_SETTLEMENT_DATE, new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date()));
        values.put(COLUMN_SETTLEMENT_STATUS, "PAID");
        return getWritableDatabase().insert(TABLE_SETTLEMENTS, null, values);
    }

    public double getSettledAmount(long tripId, long fromMemberId, long toMemberId) {
        String query = "SELECT COALESCE(SUM(" + COLUMN_SETTLEMENT_AMOUNT + "), 0) FROM " + TABLE_SETTLEMENTS
                + " WHERE " + COLUMN_SETTLEMENT_TRIP_ID + " = ? AND " + COLUMN_SETTLEMENT_FROM_MEMBER_ID + " = ? AND "
                + COLUMN_SETTLEMENT_TO_MEMBER_ID + " = ? AND " + COLUMN_SETTLEMENT_STATUS + " = 'PAID'";
        try (Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(tripId), String.valueOf(fromMemberId), String.valueOf(toMemberId)})) {
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        }
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

    // =========================================================================================
    // REMINDERS
    // =========================================================================================

    public long insertReminder(Reminder reminder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_TRIP_ID, reminder.getTripId());
        values.put(COLUMN_REMINDER_TITLE, reminder.getTitle());
        values.put(COLUMN_REMINDER_TYPE, reminder.getReminderType());
        values.put(COLUMN_REMINDER_DATE, reminder.getReminderDate());
        values.put(COLUMN_REMINDER_TIME, reminder.getReminderTime());
        values.put(COLUMN_REMINDER_NOTE, reminder.getNote());
        return db.insert(TABLE_REMINDERS, null, values);
    }

    public int updateReminder(Reminder reminder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_TRIP_ID, reminder.getTripId());
        values.put(COLUMN_REMINDER_TITLE, reminder.getTitle());
        values.put(COLUMN_REMINDER_TYPE, reminder.getReminderType());
        values.put(COLUMN_REMINDER_DATE, reminder.getReminderDate());
        values.put(COLUMN_REMINDER_TIME, reminder.getReminderTime());
        values.put(COLUMN_REMINDER_NOTE, reminder.getNote());
        return db.update(TABLE_REMINDERS, values, COLUMN_REMINDER_ID + " = ?",
                new String[]{String.valueOf(reminder.getId())});
    }

    public int deleteReminder(long reminderId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_REMINDERS, COLUMN_REMINDER_ID + " = ?", new String[]{String.valueOf(reminderId)});
    }

    public Reminder getReminder(long reminderId) {
        SQLiteDatabase db = getReadableDatabase();
        Reminder reminder = null;
        try (Cursor cursor = db.query(TABLE_REMINDERS, null, COLUMN_REMINDER_ID + " = ?",
                new String[]{String.valueOf(reminderId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                reminder = cursorToReminder(cursor);
            }
        }
        return reminder;
    }

    public List<Reminder> getRemindersByTrip(long tripId) {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String orderBy = COLUMN_REMINDER_DATE + " ASC, " + COLUMN_REMINDER_TIME + " ASC";
        String selection = COLUMN_REMINDER_TRIP_ID + " = ? AND EXISTS (SELECT 1 FROM " + TABLE_TRIPS
            + " WHERE " + TABLE_TRIPS + "." + COLUMN_TRIP_ID + " = " + TABLE_REMINDERS + "."
            + COLUMN_REMINDER_TRIP_ID + " AND " + COLUMN_TRIP_USER_ID + " = ?)";
        try (Cursor cursor = db.query(TABLE_REMINDERS, null, selection,
            new String[]{String.valueOf(tripId), String.valueOf(currentUserId())}, null, null, orderBy)) {
            while (cursor.moveToNext()) {
                reminders.add(cursorToReminder(cursor));
            }
        }
        return reminders;
    }

    /**
     * Every reminder across every trip. Used on device boot to re-schedule
     * alarms, since Android clears all AlarmManager alarms on restart.
     */
    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String orderBy = COLUMN_REMINDER_DATE + " ASC, " + COLUMN_REMINDER_TIME + " ASC";
        String selection = "EXISTS (SELECT 1 FROM " + TABLE_TRIPS + " WHERE " + TABLE_TRIPS + "."
            + COLUMN_TRIP_ID + " = " + TABLE_REMINDERS + "." + COLUMN_REMINDER_TRIP_ID
            + " AND " + COLUMN_TRIP_USER_ID + " = ?)";
        try (Cursor cursor = db.query(TABLE_REMINDERS, null, selection,
            new String[]{String.valueOf(currentUserId())}, null, null, orderBy)) {
            while (cursor.moveToNext()) {
                reminders.add(cursorToReminder(cursor));
            }
        }
        return reminders;
    }

    private Reminder cursorToReminder(Cursor cursor) {
        Reminder reminder = new Reminder();
        reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID)));
        reminder.setTripId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TRIP_ID)));
        reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TITLE)));
        reminder.setReminderType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TYPE)));
        reminder.setReminderDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_DATE)));
        reminder.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME)));
        reminder.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_NOTE)));
        return reminder;
    }
}
