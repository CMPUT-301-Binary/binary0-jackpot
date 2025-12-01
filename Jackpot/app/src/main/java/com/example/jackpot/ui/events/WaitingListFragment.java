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
            adapter = new UserArrayAdapter(
                    requireContext(),
                    userList,
                    user -> showCustomMessageDialogForUser(user)
            );
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

    private static final int MAX_MESSAGE_WORDS = 15;
    /**
     * Shows a dialog for sending a custom message to a single waiting-list user.
     * Reuses the 15-word limit behavior.
     */
    private void showCustomMessageDialogForUser(User user) {
        if (event == null) {
            Toast.makeText(requireContext(),
                    "Event data missing, cannot notify user",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter message (max " + MAX_MESSAGE_WORDS + " words)");
        input.setMaxLines(3);

        // Guard to avoid recursion when we modify text ourselves
        final boolean[] isUpdating = {false};

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isUpdating[0]) return;

                String text = s.toString();
                int wordCount = countWords(text);

                if (wordCount <= MAX_MESSAGE_WORDS) {
                    return;
                }

                String trimmed = trimToMaxWords(text, MAX_MESSAGE_WORDS);

                isUpdating[0] = true;
                input.setText(trimmed);
                input.setSelection(trimmed.length());
                isUpdating[0] = false;

                Toast.makeText(requireContext(),
                        "Maximum " + MAX_MESSAGE_WORDS + " words allowed",
                        Toast.LENGTH_SHORT).show();
            }
        };

        input.addTextChangedListener(watcher);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Notify " + user.getName())
                .setMessage("Add a custom message for this entrant:")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String customMessage = input.getText().toString().trim();
                    // Extra safety trim
                    if (countWords(customMessage) > MAX_MESSAGE_WORDS) {
                        customMessage = trimToMaxWords(customMessage, MAX_MESSAGE_WORDS);
                    }
                    sendWaitingListNotificationToUser(user, event, customMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows a dialog for the organizer to input a custom message.
     * Implements US 02.07.01 with custom message capability.
     * Enforces 15-word limit in real-time while typing.
     *  * AI Assistance:
     *  *   The structure of this method and the TextWatcher-based 15-word limit logic
     *  *   were created with help from ChatGPT (OpenAI) and then reviewed/modified.
     */
    private void showCustomMessageDialog(Event event) {
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter message (max 15 words)");
        input.setMaxLines(3);

        // We'll store the watcher in an array so we can reference it inside itself
        final android.text.TextWatcher[] watcherHolder = new android.text.TextWatcher[1];

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed; we enforce the limit in afterTextChanged
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    return;
                }

                String[] words = text.split("\\s+");
                if (words.length > 15) {
                    // Build a trimmed version with only the first 15 words
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 15; i++) {
                        if (i > 0) sb.append(' ');
                        sb.append(words[i]);
                    }

                    // Temporarily remove the watcher to avoid recursion
                    input.removeTextChangedListener(watcherHolder[0]);
                    input.setText(sb.toString());
                    // Move cursor to the end safely
                    input.setSelection(input.getText().length());
                    // Re-attach the watcher
                    input.addTextChangedListener(watcherHolder[0]);

                    Toast.makeText(requireContext(),
                            "Maximum 15 words allowed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        watcherHolder[0] = watcher;
        input.addTextChangedListener(watcher);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Notify Waiting List")
                .setMessage("Add a custom message for the waiting list entrants:")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String customMessage = input.getText().toString().trim();
                    sendWaitingListNotifications(event, customMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a waiting-list notification to a single user.
     */
    private void sendWaitingListNotificationToUser(User user, Event event, String customMessage) {
        if (user == null || user.getId() == null) {
            Toast.makeText(requireContext(),
                    "Invalid user, cannot send notification",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String payload = buildWaitingListPayload(event, customMessage);

        createNotification(
                java.util.UUID.randomUUID().toString(),
                user.getId(),
                event.getEventId(),
                "WAITING_LIST_UPDATE",
                payload,
                event.getCreatedBy()
        );

        Toast.makeText(requireContext(),
                "Sent notification to " + user.getName(),
                Toast.LENGTH_SHORT).show();
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

    // HELPERS:
    private int countWords(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        return trimmed.split("\\s+").length;
    }

    private String trimToMaxWords(String text, int maxWords) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return trimmed;

        String[] words = trimmed.split("\\s+");
        if (words.length <= maxWords) {
            return trimmed;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) sb.append(' ');
            sb.append(words[i]);
        }
        return sb.toString();
    }
}
