package com.example.jackpot.ui.map;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.jackpot.Event;
import com.example.jackpot.R;
import java.util.ArrayList;

/**
 * Adapter for displaying organizer's events in a RecyclerView.
 * Shows event poster, name, joined count, and free spots.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.EventViewHolder> {

    private static final String TAG = "OrganizerEventAdapter";
    private ArrayList<Event> events;
    private OnEventClickListener listener;

    /**
     * Interface for handling event click events.
     */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * Constructor for the adapter.
     * @param listener The click listener for event items.
     */
    public OrganizerEventAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the list of events and refreshes the RecyclerView.
     * @param events The new list of events to display.
     */
    public void setEvents(ArrayList<Event> events) {
        if (events == null) {
            this.events = new ArrayList<>();
        } else {
            this.events = events;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder for event items in the RecyclerView.
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        private ImageView eventPoster;
        private TextView eventName;
        private TextView eventJoined;
        private TextView eventFreeSpots;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventPoster = itemView.findViewById(R.id.event_poster);
            eventName = itemView.findViewById(R.id.event_name_map);
            eventJoined = itemView.findViewById(R.id.event_joined_map);
            eventFreeSpots = itemView.findViewById(R.id.event_free_spots_map);

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
        }

        /**
         * Binds event data to the view holder.
         * @param event The event to display.
         */
        public void bind(Event event) {
            if (event == null) {
                Log.e(TAG, "Event is null in bind()");
                return;
            }

            // Set event name
            eventName.setText(event.getName() != null ? event.getName() : "Unnamed Event");

            // Get waiting list size
            int joinedCount = 0;
            if (event.getWaitingList() != null) {
                joinedCount = event.getWaitingList().size();
            }

            int capacity = event.getCapacity();
            eventJoined.setText(joinedCount + "/" + capacity + " joined lottery");

            // Calculate free spots
            int freeSpots = Math.max(0, capacity - joinedCount);
            eventFreeSpots.setText(freeSpots + "/" + capacity + " spots free");

            // Load poster image using Glide
            String imageUri = event.getPosterUri();
            Log.d(TAG, "Loading image for event: " + event.getName() + ", URI: " + imageUri);

            if (imageUri != null && !imageUri.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUri)
                        .placeholder(R.drawable._ukj7h)
                        .error(R.drawable.jackpottitletext)
                        .centerCrop()
                        .into(eventPoster);
            } else {
                eventPoster.setImageResource(R.drawable._ukj7h);
            }
        }
    }
}
