package com.example.jackpot.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jackpot.Event;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaitingListFragment extends Fragment {
    private Event event;
    private RecyclerView recyclerView;
    private TextView eventTitle;
    private ImageView eventImage;
    private Button backButton;
    private Button notifyAllButton;
    private UserArrayAdapter adapter;

    public static WaitingListFragment newInstance(Event event) {
        WaitingListFragment fragment = new WaitingListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        // Get event from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        // Initialize views
        eventTitle = root.findViewById(R.id.event_title);
//        eventImage = root.findViewById(R.id.event_image);
        recyclerView = root.findViewById(R.id.waiting_list_recycler_view);
        backButton = root.findViewById(R.id.back_button);
        notifyAllButton = root.findViewById(R.id.notify_all_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set event details and populate the list
        if (event != null) {
            eventTitle.setText(event.getName());

//            if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
//                Glide.with(this).load(event.getPosterUri()).into(eventImage);
//            }

            // Manually convert Firestore's list of HashMaps into a list of User objects
            ArrayList<User> userList = new ArrayList<>();
            if (event.getWaitingList() != null && event.getWaitingList().getUsers() != null) {
                for (Object obj : event.getWaitingList().getUsers()) {
                    if (obj instanceof HashMap) {
                        try {
                            HashMap<String, Object> map = (HashMap<String, Object>) obj;
                            User user = new User();
                            user.setId((String) map.get("id"));
                            user.setName((String) map.get("name"));
                            user.setEmail((String) map.get("email"));
                            // Add any other fields you need for the User object here
                            userList.add(user);
                        } catch (Exception e) {
                            Log.e("WaitingListFragment", "Failed to convert HashMap to User", e);
                        }
                    } else if (obj instanceof User) {
                        userList.add((User) obj);
                    }
                }
            }

            // Now pass the correctly typed list to the adapter
            adapter = new UserArrayAdapter(requireContext(), userList);
            recyclerView.setAdapter(adapter);

        } else {
            Toast.makeText(getContext(), "Event data is missing.", Toast.LENGTH_SHORT).show();
        }

        // Back button
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Notify all button
        notifyAllButton.setOnClickListener(v -> {
            if (event == null || event.getWaitingList() == null || event.getWaitingList().getUsers().isEmpty()) {
                Toast.makeText(getContext(), "No entrants on the waiting list", Toast.LENGTH_SHORT).show();
                return;
            }

            showCustomMessageDialog(event);
        });

        return root;
    }

    /**
     * Sends notifications to all entrants on the waiting list.
     * Implements US 02.07.01
     */
    private void sendWaitingListNotifications(Event event, String customMessage) {
        ArrayList<User> waitingListUsers = event.getWaitingList().getUsers();

        if (waitingListUsers == null || waitingListUsers.isEmpty()) {
            Toast.makeText(getContext(), "No entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the notification payload with all required information
        String payload = buildWaitingListPayload(event, customMessage);

        int notificationCount = 0;

        // Send notification to each user on the waiting list
        for (User user : waitingListUsers) {
            if (user != null && user.getId() != null) {
                createNotification(
                        UUID.randomUUID().toString(),
                        user.getId(),
                        event.getEventId(),
                        "WAITING_LIST_UPDATE",
                        payload,
                        event.getCreatedBy()
                );
                notificationCount++;
            }
        }

        // Show success message toast to organizer
        if (notificationCount > 0) {
            Toast.makeText(getContext(),
                    "Sent notifications to " + notificationCount + " entrant(s) on the waiting list",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(),
                    "Failed to send notifications",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a dialog for the organizer to input a custom message.
     * Implements US 02.07.01 with custom message capability.
     * Enforces 15-word limit in real-time while typing.
     */
// TODO: Cite AI usage for this function.
    private void showCustomMessageDialog(Event event) {
        // Create an EditText for the dialog
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter message (max 15 words)");
        input.setMaxLines(3);

        // Add TextWatcher to enforce 15-word limit in real-time
        input.addTextChangedListener(new android.text.TextWatcher() {
            private String previousText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Count words
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    String[] words = text.split("\\s+");
                    if (words.length > 15) {
                        // Revert to previous text if word limit exceeded
                        input.setText(previousText);
                        input.setSelection(previousText.length()); // Keep cursor at end
                        Toast.makeText(getContext(),
                                "Maximum 15 words allowed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Not needed for this implementation
            }
        });

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Notify Waiting List")
                .setMessage("Add a custom message for the waiting list entrants:")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String customMessage = input.getText().toString().trim();
                    // Send notifications with custom message
                    sendWaitingListNotifications(event, customMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Builds the notification payload with all required information.
     */
    private String buildWaitingListPayload(Event event, String customMessage) {
        StringBuilder payload = new StringBuilder();

        if (customMessage != null && !customMessage.isEmpty()) {
            payload.append(customMessage);
        } else {
            payload.append("You are on the waiting list.");
        }

        return payload.toString();
    }

    /**
     * Creates a notification document and saves it to Firebase.
     */
    private void createNotification(String notificationID, String recipientID, String eventID,
                                    String notifType, String payload, String organizerID) {
        Map<String, Object> notificationDoc = new HashMap<>();
        notificationDoc.put("notificationID", notificationID);
        notificationDoc.put("recipientID", recipientID);
        notificationDoc.put("eventID", eventID);
        notificationDoc.put("notifType", notifType);
        notificationDoc.put("payload", payload);
        notificationDoc.put("organizerID", organizerID);
        notificationDoc.put("viewedByEntrant", false);

        // Save to Firebase
        com.example.jackpot.FDatabase.getInstance().addNotification(notificationDoc, notificationID);
    }
}
