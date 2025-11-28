package com.example.jackpot;

import android.content.Context;
import android.util.Log;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Locale;

/**
 * The adapter for an event array. This is used to help display the events in the event list.
 */

public class EventArrayAdapter extends ArrayAdapter<Event> {
    public enum ViewType {
        HOME,
        EVENTS
    }
    private final ViewType viewType;
    private final int layoutResource;
    private User currentUser;

    /**
     * Constructor for the adapter.
     * @param context The context of the activity.
     * @param events The list of events to display.
     * @param layoutResource The layout resource to use.
     * @param currentUser The current user.
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events, int layoutResource, ViewType type, @Nullable User currentUser) {
        super(context, 0, events);
        this.viewType = type;
        this.layoutResource = layoutResource;
        this.currentUser = currentUser;
    }

    /**
     * Set the current user.
     * @param user The user to set.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the view for the event at the given position.
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("EventArrayAdapter", "getView called for position: " + position + ", viewType: " + viewType);

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(this.layoutResource, parent, false);
            Log.d("EventArrayAdapter", "Inflated new view with layout resource: " + this.layoutResource);
        } else {
            view = convertView;
            Log.d("EventArrayAdapter", "Reusing existing view");
        }

        Event event = getItem(position);
        if (event == null) {
            Log.e("EventArrayAdapter", "Event is null at position: " + position);
            Toast.makeText(getContext(), "Event is null", Toast.LENGTH_SHORT).show();
            return view;
        }

        Log.d("EventArrayAdapter", "Setting up view for event: " + event.getName());

        // Make the entire view clickable to navigate to details
        view.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            intent.putExtra("EVENT_NAME", event.getName());
            intent.putExtra("EVENT_DESCRIPTION", event.getDescription());
            intent.putExtra("EVENT_LOCATION", event.getLocation());
            intent.putExtra("EVENT_CATEGORY", event.getCategory());
            intent.putExtra("EVENT_PRICE", event.getPrice());
            intent.putExtra("EVENT_CAPACITY", event.getCapacity());
            intent.putExtra("EVENT_DATE", event.getDate() != null ? event.getDate().getTime() : 0L);
            intent.putExtra("EVENT_REG_OPEN", event.getRegOpenAt() != null ? event.getRegOpenAt().getTime() : 0L);
            intent.putExtra("EVENT_REG_CLOSE", event.getRegCloseAt() != null ? event.getRegCloseAt().getTime() : 0L);
            intent.putExtra("EVENT_WAITING_COUNT", event.getWaitingList() != null ? event.getWaitingList().size() : 0);
            intent.putExtra("EVENT_LAT", event.getLat());
            intent.putExtra("EVENT_LNG", event.getLng());
            intent.putExtra("EVENT_GEO_REQUIRED", event.isGeoRequired());
            getContext().startActivity(intent);
        });

        // Handle the correct layout
        switch (viewType) {
            case EVENTS:
                Log.d("EventArrayAdapter", "Setting up EVENTS view type");
                setupEventView(view, event);
                break;
            case HOME:
            default:
                Log.d("EventArrayAdapter", "Setting up HOME view type");
                setupEventListItemView(view, event);
                break;
        }

        Log.d("EventArrayAdapter", "Finished setting up view for position: " + position);
        return view;
    }

    /**
     * Set up the view for an entrant event.
     * Replace your existing setupEventView method with this updated version.
     * @param view The view to set up.
     * @param event The event to set up the view for.
     */
    private void setupEventView(View view, Event event) {
        ImageView eventImage = view.findViewById(R.id.event_pic);
        TextView eventTitle = view.findViewById(R.id.event_text);
        TextView eventDetails = view.findViewById(R.id.event_details);

        eventTitle.setText(event.getName());

        String priceString = "Free";
        if (event.getPrice() != null && event.getPrice() > 0) {
            priceString = String.format(Locale.getDefault(), "$%.2f", event.getPrice());
        }

        int waitingCount = event.getWaitingCount();
        int invitedCount = event.getInvitedCount();

        String details = String.format(
                Locale.getDefault(),
                "Location: %s\nSpots: %d | Waiting: %d | Invited: %d | Price: %s",
                event.getLocation() != null ? event.getLocation() : "N/A",
                event.getCapacity(),
                waitingCount,
                invitedCount,
                priceString
        );
        eventDetails.setText(details);

        // Load the image from the database and show it. Use glide
        String imageUri = event.getPosterUri();
        if (imageUri != null && !imageUri.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUri)
                    .placeholder(R.drawable._ukj7h)
                    .error(R.drawable.jackpottitletext)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable._ukj7h);
        }

        // Check if current user is an organizer
        boolean isOrganizer = currentUser != null && currentUser.getRole() == User.Role.ORGANIZER;

        Button drawLotteryButton = view.findViewById(R.id.draw_lottery_button);
        if (drawLotteryButton != null) {
            if (!isOrganizer) {
                // should never really show for entrants, but be safe
                drawLotteryButton.setVisibility(View.GONE);
            } else {
                drawLotteryButton.setVisibility(View.VISIBLE);
                drawLotteryButton.setOnClickListener(v -> {
                    try {
                        // run the lottery for this event only
                        ArrayList<User> invitedNow = event.drawEvent();
                        // persist to Firestore
                        FDatabase.getInstance().updateEvent(event);
                        // refresh list row counts etc.
                        notifyDataSetChanged();

                        if (invitedNow.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "No entrants to invite or no capacity left.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Invited " + invitedNow.size() + " entrant(s).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Error drawing lottery: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("EventArrayAdapter", "Error in drawEvent()", e);
                    }
                });
            }
        }

        // handle Leave Button
        Button leaveButton = view.findViewById(R.id.leave_button);
        if (leaveButton != null) {
            if (isOrganizer) {
                // hide leave button for organizers
                leaveButton.setVisibility(View.GONE);
            } else {
                boolean isInWaitingList = event.hasEntrant(currentUser != null ? currentUser.getId() : null);
                if (isInWaitingList) {
                    leaveButton.setVisibility(View.VISIBLE);
                    leaveButton.setOnClickListener(v -> handleLeaveButtonClick(event));
                } else {
                    leaveButton.setVisibility(View.GONE);
                }
            }
        }

        // Handle Waiting List Button
        Button waitingListButton = view.findViewById(R.id.waiting_list_button);
        if (waitingListButton != null) {
            if (isOrganizer) {
                // For organizers, show the waiting list button
                waitingListButton.setVisibility(View.VISIBLE);
                waitingListButton.setEnabled(true);
                waitingListButton.setText("Waiting-list");
                waitingListButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#4CAF50")
                        )
                );
                waitingListButton.setOnClickListener(v -> {
                    // TODO: Add functionality later
                    Toast.makeText(getContext(), "View waiting list - TODO", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Entrant logic
                boolean isInWaitingList = event.hasEntrant(currentUser != null ? currentUser.getId() : null);

                if (isInWaitingList) {
                    waitingListButton.setEnabled(false);
                    waitingListButton.setText("Joined");
                    waitingListButton.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#9E9E9E")
                            )
                    );
                } else {
                    waitingListButton.setEnabled(true);
                    waitingListButton.setText("Waiting-list");
                    waitingListButton.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#4CAF50")
                            )
                    );

                    waitingListButton.setOnClickListener(v -> {
                        if (currentUser == null) {
                            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentUser.getRole() != User.Role.ENTRANT) {
                            Toast.makeText(getContext(), "Only entrants can join events", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Entrant entrant = new Entrant(
                                currentUser.getId(),
                                currentUser.getName(),
                                currentUser.getRole(),
                                currentUser.getEmail(),
                                currentUser.getPhone(),
                                currentUser.getProfileImageUrl(),
                                currentUser.getPassword(),
                                currentUser.getNotificationPreferences(),
                                currentUser.getDevice(),
                                currentUser.getGeoPoint()
                        );

                        try {
                            entrant.joinWaitingList(event);
                            FDatabase.getInstance().updateEvent(event);
                            Toast.makeText(getContext(), "Added to waiting list!", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("EventArrayAdapter", "Error joining waiting list", e);
                        }
                    });
                }
            }
        }

        // Handle Cancel List Button
        Button cancelListButton = view.findViewById(R.id.cancel_list_button);
        if (cancelListButton != null) {
            if (isOrganizer) {
                // For organizers, show the cancel list button
                cancelListButton.setVisibility(View.VISIBLE);
                cancelListButton.setEnabled(true);
                cancelListButton.setText("Cancel-list");
                cancelListButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#F44336")
                        )
                );
                cancelListButton.setOnClickListener(v -> {
                    // TODO: Add functionality later
                    Toast.makeText(getContext(), "View cancel list - TODO", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Entrant logic
                boolean isInWaitingList = event.hasEntrant(currentUser != null ? currentUser.getId() : null);

                if (isInWaitingList) {
                    cancelListButton.setEnabled(true);
                    cancelListButton.setVisibility(View.VISIBLE);

                    cancelListButton.setOnClickListener(v -> {
                        if (currentUser == null) {
                            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentUser.getRole() != User.Role.ENTRANT) {
                            Toast.makeText(getContext(), "Only entrants can leave events", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        User userInList = findUserInWaitingList(event, currentUser.getId());

                        if (userInList == null) {
                            Toast.makeText(getContext(), "You are not in this event's waiting list", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            event.getWaitingList().remove(userInList);
                            FDatabase.getInstance().updateEvent(event);
                            Toast.makeText(getContext(), "Removed from waiting list!", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error leaving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("EventArrayAdapter", "Error removing from waiting list", e);
                        }
                    });
                } else {
                    cancelListButton.setEnabled(false);
                    cancelListButton.setVisibility(View.GONE);
                }
            }
        }
    }


    /**
     * Set up the view for a regular event.
     * @param view The view to set up.
     * @param event The event to set up the view for.
     */
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
            String waiting = (event.getWaitingList() != null) ?
                    String.format(Locale.getDefault(), "%d waiting", event.getWaitingList().size()) : "0 waiting";
            eventWaiting.setText(waiting);
        }

        Button joinButton = view.findViewById(R.id.join_button);
        if (joinButton != null) {
            // Hide join button for organizers and admins
            if (currentUser != null &&
                    (currentUser.getRole() == User.Role.ORGANIZER || currentUser.getRole() == User.Role.ADMIN)) {
                joinButton.setVisibility(View.GONE);
            } else {
                joinButton.setVisibility(View.VISIBLE);

                // Check if user is already in waiting list
                boolean isJoined = event.hasEntrant(currentUser != null ? currentUser.getId() : null);

                if (isJoined) {
                    joinButton.setEnabled(false);
                    joinButton.setText("Joined");
                } else {
                    joinButton.setEnabled(true);
                    joinButton.setText("Join");
                }

                // Stop click propagation so button click doesn't trigger view click
                joinButton.setOnClickListener(v -> {
                    handleJoinButtonClick(event);
                });
            }
        }

        // IMPORTANT: Only load image if ImageView exists
        if (eventImage != null) {
            String imageUri = event.getPosterUri();
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

    /**
     * Handle the click event for the join button.
     * @param event The event to join.
     */
    private void handleJoinButtonClick(Event event) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getRole() == User.Role.ENTRANT) {
            Entrant entrant = new Entrant(
                    currentUser.getId(),
                    currentUser.getName(),
                    currentUser.getRole(),
                    currentUser.getEmail(),
                    currentUser.getPhone(),
                    currentUser.getProfileImageUrl(),
                    currentUser.getPassword(),
                    currentUser.getNotificationPreferences(),
                    currentUser.getDevice(),
                    currentUser.getGeoPoint()
            );
            try {
                entrant.joinWaitingList(event);
                FDatabase.getInstance().updateEvent(event);
                Toast.makeText(getContext(), "Joined waiting list!", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Only entrants can join events.", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to find the User object in the waiting list
    private User findUserInWaitingList(Event event, String userId) {
        if (event.getWaitingList() == null || userId == null) {
            return null;
        }

        ArrayList<User> users = event.getWaitingList().getUsers();
        if (users == null) {
            return null;
        }

        for (User user : users) {
            if (user != null && user.getId() != null && user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private void handleLeaveButtonClick(Event event) {

        if (currentUser.getRole() != User.Role.ENTRANT) {
            Toast.makeText(getContext(), "Only entrants can leave events.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the user in the waiting list
        User userInList = findUserInWaitingList(event, currentUser.getId());

        if (userInList == null) {
            Toast.makeText(getContext(), "You are not in this event's waiting list.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Remove the actual user object from the waiting list
            event.getWaitingList().remove(userInList);

            // Update event in database
            FDatabase.getInstance().updateEvent(event);

            Toast.makeText(getContext(), "Left waiting list successfully!", Toast.LENGTH_SHORT).show();

            // Refresh the list view
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error leaving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("EventArrayAdapter", "Error leaving event", e);
        }
    }
}