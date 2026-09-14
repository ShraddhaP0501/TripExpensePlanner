package com.example.tripexpenseplanner.adapter;

import android.text.TextUtils;
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
 * Displays the list of activities (itinerary items) for one trip on the
 * Itinerary screen. Each row lets the user Edit or Delete that activity.
 */
public class TripActivityAdapter extends RecyclerView.Adapter<TripActivityAdapter.ActivityViewHolder> {

    /**
     * Callback used by ItineraryActivity to react to Edit/Delete taps.
     */
    public interface OnActivityActionListener {
        void onEditActivity(TripActivity activity);

        void onDeleteActivity(TripActivity activity);
    }

    private List<TripActivity> activities = new ArrayList<>();
    private final OnActivityActionListener listener;

    public TripActivityAdapter(OnActivityActionListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<TripActivity> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        TripActivity activity = activities.get(position);

        holder.textActivityName.setText(activity.getActivityName());

        String dateTime = activity.getActivityDate();
        if (!TextUtils.isEmpty(activity.getActivityTime())) {
            dateTime = dateTime + ", " + activity.getActivityTime();
        }
        holder.textActivityDateTime.setText(dateTime);

        String description = activity.getDescription();
        boolean hasDescription = !TextUtils.isEmpty(description);
        holder.textActivityDescription.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        holder.textActivityDescription.setText(description);

        holder.buttonEditActivity.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditActivity(activity);
            }
        });
        holder.buttonDeleteActivity.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteActivity(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        final TextView textActivityName;
        final TextView textActivityDateTime;
        final TextView textActivityDescription;
        final View buttonEditActivity;
        final View buttonDeleteActivity;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            textActivityName = itemView.findViewById(R.id.textActivityName);
            textActivityDateTime = itemView.findViewById(R.id.textActivityDateTime);
            textActivityDescription = itemView.findViewById(R.id.textActivityDescription);
            buttonEditActivity = itemView.findViewById(R.id.buttonEditActivity);
            buttonDeleteActivity = itemView.findViewById(R.id.buttonDeleteActivity);
        }
    }
}
