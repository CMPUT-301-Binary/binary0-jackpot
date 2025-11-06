package com.example.jackpot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final int layoutResource;
    private FirebaseUser currentUser;
    private User.Role userRole;

    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource, User.Role role) {
        super(context, 0, events);
        this.layoutResource = layoutResource;
        this.userRole = role;
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

        // Check if we are using the new entrant_event_content.xml layout
        // by looking for a view ID that is unique to it.
        TextView eventDetails = view.findViewById(R.id.event_details);
        if (eventDetails != null) {
            // New layout (`entrant_event_content.xml`)
            ImageView eventImage = view.findViewById(R.id.eventPic);
            TextView eventTitle = view.findViewById(R.id.event_text);

            // TODO: Use a library like Glide or Picasso to load the image from a URL
            // if (event.getPosterImage() != null) {
            //     Glide.with(getContext()).load(event.getPosterImage().getUrl()).into(eventImage);
            // }

            eventTitle.setText(event.getTitle());

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

        } else {
            // Fallback to the old layout logic
            TextView eventCategory = view.findViewById(R.id.event_category);
            TextView eventName = view.findViewById(R.id.event_name);
            TextView eventPrice = view.findViewById(R.id.event_price);
            TextView eventSpots = view.findViewById(R.id.event_spots);
            TextView eventWaiting = view.findViewById(R.id.event_waiting);

            // TODO: Use a library like Glide or Picasso to load the image from a URL
            // if (event.getPosterImage() != null) { ... }

            if (eventCategory != null) {
                eventCategory.setText(event.getCategory());
            }
            if (eventName != null) {
                eventName.setText(event.getTitle());
            }

            if (eventPrice != null) {
                if (event.getPrice() != null && event.getPrice() > 0) {
                    eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
                } else {
                    eventPrice.setText("Free");
                }
            }

            if (eventSpots != null) {
                String spotsText = String.format(Locale.getDefault(), "%d spots", event.getCapacity());
                eventSpots.setText(spotsText);
            }
            if (eventWaiting != null) {
                String waiting;
                if (event.getWaitingList() != null) {
                    waiting = String.format(Locale.getDefault(), "%d waiting", event.getWaitingList().size());
                } else {
                    waiting = "0 waiting";
                }
                eventWaiting.setText(waiting);
            }
        }
        if (userRole == User.Role.ORGANIZER || userRole == User.Role.ADMIN) {
            joinButton.setVisibility(View.GONE);
        } else {
            joinButton.setVisibility(View.VISIBLE);
        }
        return view;
    }
}
