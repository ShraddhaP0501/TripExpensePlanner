package com.example.tripexpenseplanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.model.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of trips on the My Trips screen.
 * Tapping a row notifies the activity via {@link OnTripClickListener} so it can
 * open Trip Details for that trip's id.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    /**
     * Callback used by MyTripsActivity to react to row taps.
     */
    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    private List<Trip> trips = new ArrayList<>();
    private final OnTripClickListener listener;

    public TripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips != null ? trips : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.textTripName.setText(trip.getTripName());
        holder.textTripDestination.setText(trip.getDestination());

        String dateRange = trip.getStartDate() + " to " + trip.getEndDate();
        holder.textTripDates.setText(dateRange);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTripClick(trip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        final TextView textTripName;
        final TextView textTripDestination;
        final TextView textTripDates;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            textTripName = itemView.findViewById(R.id.textTripName);
            textTripDestination = itemView.findViewById(R.id.textTripDestination);
            textTripDates = itemView.findViewById(R.id.textTripDates);
        }
    }
}
