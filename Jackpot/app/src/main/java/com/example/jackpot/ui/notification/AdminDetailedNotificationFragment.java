package com.example.jackpot.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment that displays detailed notifications for a specific organizer.
 */
public class AdminDetailedNotificationFragment extends Fragment {

    private static final String TAG = "DetailedNotification";
    private static final String ARG_ORGANIZER_ID = "organizer_id";
    private static final String ARG_ORGANIZER_NAME = "organizer_name";
    private static final String ARG_ORGANIZER_EMAIL = "organizer_email";

    private String organizerId;
    private String organizerName;
    private String organizerEmail;

    private RecyclerView recyclerView;
    private AdminNotificationDetailAdapter adapter;
    private FirebaseFirestore db;
    private TextView headerName;
    private ImageButton backButton;

    /**
     * Creates a new instance of this fragment with organizer information.
     * @param organizerId The ID of the organizer
     * @param organizerName The name of the organizer
     * @param organizerEmail The email of the organizer
     * @return A new instance of AdminDetailedNotificationFragment
     */
    public static AdminDetailedNotificationFragment newInstance(String organizerId, String organizerName, String organizerEmail) {
        AdminDetailedNotificationFragment fragment = new AdminDetailedNotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORGANIZER_ID, organizerId);
        args.putString(ARG_ORGANIZER_NAME, organizerName);
        args.putString(ARG_ORGANIZER_EMAIL, organizerEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organizerId = getArguments().getString(ARG_ORGANIZER_ID);
            organizerName = getArguments().getString(ARG_ORGANIZER_NAME);
            organizerEmail = getArguments().getString(ARG_ORGANIZER_EMAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detailed_notification_admin, container, false);

        db = FirebaseFirestore.getInstance();

        // Set up header with organizer name
        headerName = root.findViewById(R.id.header_organizer_name);
        if (headerName != null && organizerName != null) {
            headerName.setText(organizerName + "'s Notifications");
        }

        // Set up back button
        backButton = root.findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Pop back stack to return to organizer list
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        // Set up RecyclerView
        recyclerView = root.findViewById(R.id.detailed_notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter
        adapter = new AdminNotificationDetailAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load notifications for this organizer
        loadNotificationsForOrganizer();

        return root;
    }

    /**
     * Loads all notifications sent by the selected organizer.
     */
    private void loadNotificationsForOrganizer() {
        Log.d(TAG, "Loading notifications for organizer: " + organizerId);

        db.collection("notifications")
                .whereEqualTo("organizerID", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationDetail> notifications = new ArrayList<>();

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " notifications");

                    if (queryDocumentSnapshots.isEmpty()) {
                        // Check if fragment is still attached before showing Toast
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(),
                                    "No notifications found for " + organizerName,
                                    Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    AtomicInteger processedCount = new AtomicInteger(0);
                    int totalCount = queryDocumentSnapshots.size();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String notificationId = document.getId();
                            String eventId = document.getString("eventID");
                            String recipientId = document.getString("recipientID");
                            String notifType = document.getString("notifType");
                            String payload = document.getString("payload");

                            Log.d(TAG, "Processing notification: " + payload);

                            if (payload != null) {
                                // Create notification detail object
                                NotificationDetail notification = new NotificationDetail(
                                        notificationId,
                                        eventId,
                                        recipientId,
                                        notifType,
                                        payload
                                );

                                // Fetch event name if eventId exists
                                if (eventId != null && !eventId.isEmpty()) {
                                    fetchEventName(notification, eventId, notifications, processedCount, totalCount);
                                } else {
                                    notification.setEventName("No Event");
                                    notifications.add(notification);
                                    checkIfAllProcessed(notifications, processedCount.incrementAndGet(), totalCount);
                                }
                            } else {
                                processedCount.incrementAndGet();
                                checkIfAllProcessed(notifications, processedCount.get(), totalCount);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing notification", e);
                            processedCount.incrementAndGet();
                            checkIfAllProcessed(notifications, processedCount.get(), totalCount);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications", e);
                    // Check if fragment is still attached before showing Toast
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fetches the event name for a notification.
     */
    private void fetchEventName(NotificationDetail notification, String eventId,
                                List<NotificationDetail> notifications,
                                AtomicInteger processedCount, int totalCount) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name");
                        notification.setEventName(eventName != null ? eventName : "Unknown Event");
                    } else {
                        notification.setEventName("Event Not Found");
                    }
                    notifications.add(notification);
                    checkIfAllProcessed(notifications, processedCount.incrementAndGet(), totalCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching event name", e);
                    notification.setEventName("Error Loading Event");
                    notifications.add(notification);
                    checkIfAllProcessed(notifications, processedCount.incrementAndGet(), totalCount);
                });
    }

    /**
     * Checks if all notifications have been processed and updates the adapter.
     */
    private void checkIfAllProcessed(List<NotificationDetail> notifications, int processed, int total) {
        Log.d(TAG, "Processed " + processed + " of " + total + " notifications");
        if (processed == total) {
            Log.d(TAG, "All notifications processed, updating adapter with " + notifications.size() + " items");
            adapter.updateData(notifications);
        }
    }

    /**
     * Data class to hold notification details.
     */
    public static class NotificationDetail {
        private String notificationId;
        private String eventId;
        private String recipientId;
        private String notifType;
        private String payload;
        private String eventName;
        private String recipientName;

        public NotificationDetail(String notificationId, String eventId, String recipientId,
                                  String notifType, String payload) {
            this.notificationId = notificationId;
            this.eventId = eventId;
            this.recipientId = recipientId;
            this.notifType = notifType;
            this.payload = payload;
            this.eventName = "Loading...";
            this.recipientName = "Loading...";
        }

        public String getNotificationId() {
            return notificationId;
        }

        public String getEventId() {
            return eventId;
        }

        public String getRecipientId() {
            return recipientId;
        }

        public String getNotifType() {
            return notifType;
        }

        public String getPayload() {
            return payload;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getRecipientName() {
            return recipientName;
        }

        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }
    }
}