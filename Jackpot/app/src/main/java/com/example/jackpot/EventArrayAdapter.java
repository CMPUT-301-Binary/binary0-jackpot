package com.example.jackpot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final int layoutResource;
    private User currentUser;

    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource) {
        super(context, 0, events);
        this.layoutResource = layoutResource;
    }

    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource, User user) {
        super(context, 0, events);
        this.layoutResource = layoutResource;
        this.currentUser = user;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(this.layoutResource, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        if (event == null) {
            return view; // Return the recycled view as-is if there's no data
        }

        // Handle the new layout (entrant_event_content.xml)
        TextView eventDetails = view.findViewById(R.id.event_details);
        if (eventDetails != null) {
            // This is the entrant_event_content.xml layout
            setupEntrantEventView(view, event);
        } else {
            // This is the event_list_item.xml layout
            setupEventListItemView(view, event);
        }

        return view;
    }

    private void setupEntrantEventView(View view, Event event) {
        ImageView eventImage = view.findViewById(R.id.eventPic);
        TextView eventTitle = view.findViewById(R.id.event_text);
        TextView eventDetails = view.findViewById(R.id.event_details);

        eventTitle.setText(event.getName());

        String priceString = "Free";
        if (event.getPrice() != null && event.getPrice() > 0) {
            priceString = String.format(Locale.getDefault(), "$%.2f", event.getPrice());
        }

        int waitingCount = 0;
        if (event.getWaitingList() != null) {
            waitingCount = event.getWaitingList().size();
        }

        String details = String.format(Locale.getDefault(), "Location: %s\nSpots: %d | Waiting: %d | Price: %s",
                event.getLocationAddress() != null ? event.getLocationAddress() : "N/A",
                event.getCapacity(),
                waitingCount,
                priceString);
        eventDetails.setText(details);
    }

    private void setupEventListItemView(View view, Event event) {
        TextView eventName = view.findViewById(R.id.event_name);
        if (eventName != null) {
            eventName.setText(event.getName());
        }

        TextView eventCategory = view.findViewById(R.id.event_category);
        if (eventCategory != null) {
            eventCategory.setText(event.getCategory());
        }

        TextView eventPrice = view.findViewById(R.id.event_price);
        if (eventPrice != null) {
            if (event.getPrice() != null && event.getPrice() > 0) {
                eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
            } else {
                eventPrice.setText("Free");
            }
        }

        TextView eventSpots = view.findViewById(R.id.event_spots);
        if (eventSpots != null) {
            String spotsText = String.format(Locale.getDefault(), "%d spots", event.getCapacity());
            eventSpots.setText(spotsText);
        }

        TextView eventWaiting = view.findViewById(R.id.event_waiting);
        if (eventWaiting != null) {
            String waiting = (event.getWaitingList() != null) ? String.format(Locale.getDefault(), "%d waiting", event.getWaitingList().size()) : "0 waiting";
            eventWaiting.setText(waiting);
        }

        Button joinButton = view.findViewById(R.id.join_button);
        if (joinButton != null) {
            joinButton.setOnClickListener(v -> {
                if (currentUser instanceof Entrant) {
                    Entrant entrant = (Entrant) currentUser;
                    try {
                        entrant.joinWaitingList(event);
                        FDatabase.getInstance().updateEvent(event);
                        Toast.makeText(getContext(), "Joined waiting list!", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged(); // To update the waiting count
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Only entrants can join events.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
