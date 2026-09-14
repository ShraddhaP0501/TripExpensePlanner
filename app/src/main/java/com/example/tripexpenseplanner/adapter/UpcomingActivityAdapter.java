package com.example.tripexpenseplanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.model.TripActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a simple read-only list of upcoming activities on the Dashboard.
 * The list is populated later, once trip/activity creation is implemented —
 * for now it is fed an empty list by MainActivity.
 */
public class UpcomingActivityAdapter extends RecyclerView.Adapter<UpcomingActivityAdapter.ActivityViewHolder> {

    private List<TripActivity> activities = new ArrayList<>();

    public void setActivities(List<TripActivity> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        TripActivity activity = activities.get(position);
        holder.textActivityName.setText(activity.getActivityName());

        String dateTime = activity.getActivityDate();
        if (activity.getActivityTime() != null && !activity.getActivityTime().isEmpty()) {
            dateTime = dateTime + ", " + activity.getActivityTime();
        }
        holder.textActivityDateTime.setText(dateTime);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        final TextView textActivityName;
        final TextView textActivityDateTime;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            textActivityName = itemView.findViewById(R.id.textActivityName);
            textActivityDateTime = itemView.findViewById(R.id.textActivityDateTime);
        }
    }
}
