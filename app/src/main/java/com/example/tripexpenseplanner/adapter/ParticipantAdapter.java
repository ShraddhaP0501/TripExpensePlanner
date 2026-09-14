package com.example.tripexpenseplanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.model.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of participants for one trip on the Participants screen.
 * Each row lets the user Edit or Delete that participant.
 */
public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    /**
     * Callback used by ParticipantsActivity to react to Edit/Delete taps.
     */
    public interface OnParticipantActionListener {
        void onEditParticipant(Participant participant);

        void onDeleteParticipant(Participant participant);
    }

    private List<Participant> participants = new ArrayList<>();
    private final OnParticipantActionListener listener;

    public ParticipantAdapter(OnParticipantActionListener listener) {
        this.listener = listener;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants != null ? participants : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Participant participant = participants.get(position);

        holder.textParticipantName.setText(participant.getName());

        holder.buttonEditParticipant.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditParticipant(participant);
            }
        });
        holder.buttonDeleteParticipant.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteParticipant(participant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        final TextView textParticipantName;
        final View buttonEditParticipant;
        final View buttonDeleteParticipant;

        ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            textParticipantName = itemView.findViewById(R.id.textParticipantName);
            buttonEditParticipant = itemView.findViewById(R.id.buttonEditParticipant);
            buttonDeleteParticipant = itemView.findViewById(R.id.buttonDeleteParticipant);
        }
    }
}
