package com.example.jackpot;

import android.content.Context;
import android.util.Log;
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

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private final int layoutResource;
    private User currentUser;

    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource, @Nullable User currentUser) {
        super(context, 0, events);
        this.layoutResource = layoutResource;
        this.currentUser = null;
    }

    //    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource, User user) {
//        super(context, 0, events);
//        this.layoutResource = layoutResource;
//        this.currentUser = user;
//    }
    public void setCurrentUser(User user) {
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
            Toast.makeText(getContext(), "Event is null", Toast.LENGTH_SHORT).show();
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
                event.getLocation() != null ? event.getLocation() : "N/A",
                event.getCapacity(),
                waitingCount,
                priceString);
        eventDetails.setText(details);

        //Load the image from the database and show it. Use glide
        String imageUri = event.getPosterUri();
        //String imageUri = "https://firebasestorage.googleapis.com/v0/b/jackpot-d3153.firebasestorage.app/o/posters%2F00aec71a-f834-4b8c-8c9b-15391d89583b.png?alt=media&token=ffeded25-46b1-4a17-ae17-5b3e414b1b8f";
        //Log imageUri for debugging
        Log.d("EventArrayAdapter", "Image URI: " + details);
        if (imageUri != null && !imageUri.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUri)
                    .placeholder(R.drawable._ukj7h)
                    .error(R.drawable.jackpottitletext)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable._ukj7h);
        }


    }

    private void setupEventListItemView(View view, Event event) {
        TextView eventName = view.findViewById(R.id.event_name);
        ImageView eventImage = view.findViewById(R.id.event_image);

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
                if (currentUser.getRole() == User.Role.ENTRANT) {
                    Entrant entrant = new Entrant(
                            currentUser.getId(),
                            currentUser.getName(),
                            currentUser.getRole(),
                            currentUser.getEmail(),
                            currentUser.getPhone(),
                            currentUser.getPassword(),
                            currentUser.getNotificationPreferences(),
                            currentUser.getDevice()
                    );
                    try {
                        entrant.joinWaitingList(event);
                        FDatabase.getInstance().updateEvent(event);
                        Toast.makeText(getContext(), "Joined waiting list!", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged(); // To update the waiting count
                    } catch (Exception e) {
//                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else if (currentUser != null) {
                    Toast.makeText(getContext(), "Only entrants can join events.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No user logged in.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Load the image from the database and show it. Use glide
        String imageUri = event.getPosterUri();
        //String imageUri = "https://firebasestorage.googleapis.com/v0/b/jackpot-d3153.firebasestorage.app/o/posters%2F00aec71a-f834-4b8c-8c9b-15391d89583b.png?alt=media&token=ffeded25-46b1-4a17-ae17-5b3e414b1b8f";
        //Log imageUri for debugging
        if (imageUri != null && !imageUri.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUri)
                    .placeholder(R.drawable._ukj7h)
                    .error(R.drawable.jackpottitletext)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable._ukj7h);
        }
    }
}
