package com.example.tripexpenseplanner;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires the shared bottom navigation bar (Home / Trips / Add Trip / Expenses) that
 * appears at the bottom of every screen in the app. Centralized here so the same
 * behavior doesn't have to be copied into every Activity by hand.
 *
 * Each Activity calls {@link #setup} once in onCreate (passing whichever nav item
 * best represents that screen) and {@link #resetSelection} in onResume, so
 * returning from a screen that changed the selected tab doesn't leave a stale
 * tab highlighted.
 */
public final class NavigationHelper {

    private NavigationHelper() {
        // Static helper class — not meant to be instantiated.
    }

    public static void setup(Activity activity, int currentItemId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) {
            return;
        }
        bottomNavigation.setSelectedItemId(currentItemId);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentItemId) {
                // Already on the screen this tab represents.
                return true;
            }
            if (itemId == R.id.navHome) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return true;
            } else if (itemId == R.id.navMyTrips) {
                activity.startActivity(new Intent(activity, MyTripsActivity.class));
                return true;
            } else if (itemId == R.id.navAddTrip) {
                activity.startActivity(new Intent(activity, AddTripActivity.class));
                return true;
            } else if (itemId == R.id.navExpenses) {
                Toast.makeText(activity, R.string.msg_feature_coming_soon, Toast.LENGTH_SHORT).show();
                // Returning false keeps the current tab highlighted — there's no
                // trip-independent Expenses screen to switch to from here.
                return false;
            }
            return false;
        });
    }

    /**
     * Re-applies the correct selected tab. Call from onResume: without this, a
     * screen that was left showing a different tab selected (because the user
     * tapped it and this Activity started, then they pressed Back) would keep
     * showing that stale selection instead of its own tab.
     */
    public static void resetSelection(Activity activity, int currentItemId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(currentItemId);
        }
    }
}
