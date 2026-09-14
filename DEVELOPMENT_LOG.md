# Trip Expense and Planner — Development Log

A native Android application (Java, Android XML, SQLite, Gradle) built incrementally,
step by step. This log records what was built at each stage, the key files involved,
and how the major features work.

**Package:** `com.example.tripexpenseplanner`
**Tech stack:** Java · Android XML · SQLite (`SQLiteOpenHelper`, no Room) · Gradle
**Explicitly not used:** Flutter, Dart, Firebase, PHP, Laravel, external/server APIs

---

## Step 1 — Project Scaffold

Created the standard Android project structure from scratch: Gradle wrapper, root and
app `build.gradle`, `AndroidManifest.xml`, and a `MainActivity` showing a simple
"Trip Expense and Planner" welcome screen. Established the base resources
(`colors.xml`, `strings.xml`, `themes.xml`, launcher icon) and confirmed the project
opens in Android Studio and is editable in VS Code.

**Key files:** `settings.gradle`, `build.gradle`, `app/build.gradle`,
`AndroidManifest.xml`, `MainActivity.java`, `activity_main.xml`

---

## Step 2 — SQLite Database Layer

Implemented `DatabaseHelper` (`SQLiteOpenHelper`) defining the full schema up front:

| Table | Purpose |
|---|---|
| `trips` | One row per trip (name, destination, start/end date, notes) |
| `activities` | Itinerary items, `FOREIGN KEY(trip_id)` |
| `expenses` | Money spent, `FOREIGN KEY(trip_id)` |
| `participants` | People on a trip, `FOREIGN KEY(trip_id)` |
| `expense_participants` | Per-expense splits, `FOREIGN KEY`s to both `expenses` and `participants` |

Every table got full Insert/Update/Delete/Select methods in `DatabaseHelper`, plus
matching model classes (`Trip`, `TripActivity`, `Expense`, `Participant`,
`ExpenseParticipant`). Foreign key enforcement is turned on via
`setForeignKeyConstraintsEnabled(true)`.

**Key files:** `database/DatabaseHelper.java`, `model/*.java`

---

## Step 3 — Dashboard UI

Replaced the placeholder welcome screen with the real Dashboard: app title, a Total
Expenses summary card, an "Add New Trip" button, "My Trips"/"Expenses" buttons, and an
"Upcoming Activities" preview list (`RecyclerView`, empty-state aware). Buttons for
features not yet built showed a "coming soon" Toast.

**Key files:** `activity_main.xml`, `MainActivity.java`,
`adapter/UpcomingActivityAdapter.java`, `item_upcoming_activity.xml`, `dimens.xml`

---

## Step 4 — Create Trip

Built `AddTripActivity`: Trip Name, Destination, Start/End Date (`DatePickerDialog`),
Notes. Validates required fields and that start date ≤ end date, inserts into
`trips` via `DatabaseHelper.insertTrip()`, and shows success/error feedback.

**Key files:** `AddTripActivity.java`, `activity_add_trip.xml`

---

## Step 5 — My Trips (List / View / Edit / Delete)

- `MyTripsActivity`: loads all trips (`getAllTrips()`), displays them in a
  `RecyclerView` via `TripAdapter`.
- `TripDetailsActivity`: shows one trip's full details; Edit routes back into
  `AddTripActivity` in an "edit mode" (pre-filled, calls `updateTrip()` instead of
  `insertTrip()`); Delete shows a confirmation dialog, then `deleteTrip()`.
- Navigation always passes the trip's `id` (a `long`) via Intent extras — every
  screen re-reads the current row from SQLite by that id rather than passing whole
  objects around, so edits are always reflected immediately.

**Key files:** `MyTripsActivity.java`, `TripDetailsActivity.java`,
`adapter/TripAdapter.java`, `item_trip.xml`, `activity_my_trips.xml`,
`activity_trip_details.xml`

---

## Step 6 — Trip Details Navigation

Added four section buttons to Trip Details — Itinerary, Expenses, Participants, Trip
Summary — each passing `trip_id` onward. Since none of those features existed yet,
all four opened one shared `PlaceholderActivity` ("coming soon" + the trip id, to
confirm the navigation wiring). Replaced one at a time as each feature was built in
later steps.

**Key files:** `PlaceholderActivity.java`, `activity_placeholder.xml`

---

## Step 7 — Itinerary

`ItineraryActivity` lists activities for one trip (ordered by
`activity_date, activity_time`), with Add/Edit/Delete. `AddEditActivityActivity`
handles both create and edit, using `DatePickerDialog` and `TimePickerDialog`.
Validates Activity Name and Date as required.

**Key files:** `ItineraryActivity.java`, `AddEditActivityActivity.java`,
`adapter/TripActivityAdapter.java`, `item_activity.xml`, `activity_itinerary.xml`,
`activity_add_edit_activity.xml`

---

## Step 8 — Expense Management

`ExpensesActivity` lists expenses per trip and shows their live total (SQL
`SUM(amount)` via `getTotalExpenseForTrip()`). `AddEditExpenseActivity` handles
Category (fixed list: Food, Transportation, Accommodation, Shopping, Activities,
Other), Amount, Paid By, Description, Date — validating category selection, a
positive numeric amount, and required date.

**Key files:** `ExpensesActivity.java`, `AddEditExpenseActivity.java`,
`adapter/ExpenseAdapter.java`, `item_expense.xml`, `activity_expenses.xml`,
`activity_add_edit_expense.xml`, `arrays.xml` (`expense_categories`)

---

## Step 9 — Trip Participants

`ParticipantsActivity` + `AddEditParticipantActivity`: simple Add/View/Edit/Delete
for the people on a trip (`participants` table, one field — Name). Sets up the
roster that Step 10's expense splitting depends on.

**Key files:** `ParticipantsActivity.java`, `AddEditParticipantActivity.java`,
`adapter/ParticipantAdapter.java`, `item_participant.xml`,
`activity_participants.xml`, `activity_add_edit_participant.xml`

---

## Step 10 — Group Expense Splitting

Extended the Expense form with a "Paid By" participant picker and a "Split Among"
checklist. On save, the amount is divided **equally** among selected participants
using **integer cents** (not floating point), so shares always sum exactly to the
total — any leftover cent from an uneven split goes to the first participant(s) in
list order (e.g. ₹1000 ÷ 3 → ₹333.34 / ₹333.33 / ₹333.33). Each share is stored as
one row in `expense_participants`.

Added a "Who Owes Whom" settlement view (in `TripSummaryActivity`): balances
(`paid − share`) are netted per participant, then a simple greedy match pairs the
largest creditor with the largest debtor repeatedly — also computed in integer
cents to guarantee balances land on exactly zero.

**Key files:** `AddEditExpenseActivity.java` (extended), `TripSummaryActivity.java`,
`adapter/ParticipantBalanceAdapter.java`, `model/ParticipantBalance.java`,
`model/SettlementEntry.java`, `item_participant_balance.xml`

---

## Step 11 — Trip Summary (Overview + Statistics)

Extended Trip Summary with a full read-only overview, everything computed live from
SQLite:

- **Trip Overview** — name, destination, date range, and **duration** (`(end − start
  in days) + 1`, counting both endpoints).
- **Statistics** — activity count, expense count, participant count, total expense.
- **Category-wise totals** — one SQL query: `SELECT category, SUM(amount) FROM
  expenses WHERE trip_id = ? GROUP BY category ORDER BY SUM(amount) DESC`.

**Key files:** `TripSummaryActivity.java` (extended), `DatabaseHelper.java`
(`getCategoryWiseExpenseTotals`), `model/CategoryTotal.java`

---

## Step 12 — Local Reminders

Added a `reminders` table and full CRUD, plus a notification system built entirely
on Android framework APIs — **no Firebase, no server**:

- `AddReminderActivity` — Title, Type (Activity / Hotel Booking / Transportation
  Booking / Important Note), Date, 24-hour Time, optional Note. Rejects times in
  the past.
- `ReminderScheduler` — combines date + time into a trigger timestamp and calls
  `AlarmManager.setExactAndAllowWhileIdle(...)`, falling back to an inexact `set()`
  if exact-alarm permission isn't granted (Android 12+).
- `ReminderReceiver` (`BroadcastReceiver`) — fires when the alarm goes off, builds
  and shows a `NotificationCompat` notification (even if the app isn't running),
  and opens `TripDetailsActivity` for that trip when tapped.
- `BootReceiver` — re-schedules every future reminder after a device restart
  (`AlarmManager` alarms don't survive a reboot on their own).
- `RemindersActivity` requests `POST_NOTIFICATIONS` at runtime on Android 13+.

**Key files:** `RemindersActivity.java`, `AddReminderActivity.java`,
`notification/ReminderScheduler.java`, `notification/ReminderReceiver.java`,
`notification/BootReceiver.java`, `adapter/ReminderAdapter.java`,
`model/Reminder.java`

---

## Step 13 — UI Polish Pass

A visual-only pass across the whole app — **no logic changes**:

- **Colors** — every hardcoded `#C62828` (and duplicated hex in Java) replaced with
  a proper `@color/danger` resource.
- **Buttons** — one global `AppButton` style (via the theme's `materialButtonStyle`)
  gives every button the same rounded corners and turns off Material's default
  ALL-CAPS text; small in-row actions (Edit/Delete/Cancel) use a matching
  `AppButton.Borderless` style.
- **Text sizes / spacing** — scattered `12sp`/`13sp`/`14sp` and `2dp`/`4dp` literals
  consolidated into named dimens.
- **Dialogs** — `AlertDialog.Builder` → `MaterialAlertDialogBuilder` everywhere,
  for a properly Material-styled look.
- **Back navigation** — every screen except the Dashboard now has a visible back
  arrow in its header bar, wired to `finish()`.
- **Empty states** — all "No X yet" messages given a consistent text size.

37 files touched, zero new dependencies.

**Key files:** `themes.xml`, `colors.xml`, `dimens.xml`, `ic_back_arrow.xml`, and
every screen's layout + host Activity.

---

## Step 14 — Dashboard Bottom Navigation

Added a persistent 4-tab `BottomNavigationView` to the Dashboard: **Home**, **Trips**,
**Add Trip**, **Expenses**. Home is a no-op (already there); Trips and Add Trip mirror
the Dashboard's existing buttons; Expenses has no trip-independent screen yet, so it
shows the same "coming soon" message as the Dashboard's own Expenses button, and
declines to highlight itself (stays on "Home") since there's no matching screen
underneath it. Scoped to the Dashboard only, since every other screen in the app is
already trip-specific (needs a `trip_id`) and wouldn't have a consistent set of 4
destinations to offer.

**Key files:** `activity_main.xml`, `MainActivity.java`, `menu/bottom_nav_menu.xml`,
`color/bottom_nav_item_color.xml`, `drawable/ic_nav_home.xml`,
`drawable/ic_nav_trips.xml`, `drawable/ic_nav_add.xml`, `drawable/ic_nav_expenses.xml`

---

## Database Schema Summary

```
trips (id, trip_name, destination, start_date, end_date, notes)
  │
  ├── activities (id, trip_id→trips, activity_name, activity_date, activity_time, description)
  ├── participants (id, trip_id→trips, name)
  └── expenses (id, trip_id→trips, category, amount, paid_by, description, expense_date)
        │
        └── expense_participants (id, expense_id→expenses, participant_id→participants, share_amount)

reminders (id, trip_id→trips, title, reminder_type, reminder_date, reminder_time, note)
```

## Known Simplifications (documented, not bugs)

- `expenses.paid_by` stores a participant's **name** as text, not a foreign key —
  balance calculations match by name, so renaming a participant won't relabel who
  paid on past expenses.
- The settlement ("who owes whom") algorithm is a simple greedy match — correct,
  but not guaranteed to produce the mathematically minimal number of payments.
- `DatabaseHelper.onUpgrade()` drops and recreates all tables (documented in code) —
  fine at this project stage, not a production-ready migration strategy.
- Deleting a Trip does not cascade-delete its activities/expenses/participants —
  noted as a possible follow-up if that scenario is exercised.
