package com.example.jackpot.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.Event;

import com.example.jackpot.FDatabase;
import com.example.jackpot.MainActivity;
import com.example.jackpot.Notification;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.example.jackpot.ui.map.OrganizerEventAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification fragment, which will show the notifications of the user.
 * For ADMIN role, displays all organizers (name and email) from the users collection.
 * Created with the assistance of ClaudeAI Sonnet 4.5
 *
 * Responsibilities:
 *  - Inflate role-specific notification UIs (entrant/organizer/admin).
 *  - Entrant: load unread notifications, long-press dismiss.
 *  - Admin: list organizers with contact info.
 */
public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";
    private OrganizerEventAdapter organizerEventAdapter;
    private RecyclerView recyclerView;
    private AdminNotificationAdapter adapter;
    private FirebaseFirestore db;

    //For entrant notification
    private ListView notificationListView;
    private EntrantNotificationAdapter notificationAdapter;
    private ArrayList<Notification> notificationList;
    private FDatabase fDatabase;
    private String currentUserId;


    /**
     * Inflate the role-specific layout and initialize role-specific controllers.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Get user role
        User.Role role = ((MainActivity) requireActivity()).getCurrentUserRole();

        if (role == null) {
            role = User.Role.ENTRANT;
        }

        // Inflate correct layout based on role
        View root;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_notification_organizer, container, false);
                setupOrganizerNotifications(root);
                break;
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_notification_admin, container, false);
                setupAdminNotifications(root);
                break;
            default:
                root = inflater.inflate(R.layout.fragment_notification_entrant, container, false);
                setupEntrantNotifications(root);
                break;
        }

        return root;
    }

    private void setupEntrantNotifications(View root){
        //Get current user ID from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUserId = currentUser.getUid();
        fDatabase = FDatabase.getInstance();
        notificationListView = root.findViewById(R.id.notificationListView);
        notificationList = new ArrayList<>();
        notificationAdapter = new EntrantNotificationAdapter(getContext(), notificationList);
        notificationListView.setAdapter(notificationAdapter);

        setupLongPressToDismiss();
        loadUnreadNotifications();

    }
    /** Loads only unread notifications for the current entrant. */
    private void loadUnreadNotifications() {
        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            return;
        }
        Log.d(TAG, "Loading unread notifications for user: " + currentUserId);

        // Query notifications where recipientID matches AND read is false (or doesn't exist)
        db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("recipientID", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Notification> unreadNotifications = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Boolean isRead = document.getBoolean("viewedByEntrant");
                        // If 'read' field doesn't exist or is false, include it
                        if (isRead == null || !isRead) {
                            Notification notification = document.toObject(Notification.class);
                            if (notification != null) {
                                notification.setNotificationID(document.getId());
                                unreadNotifications.add(notification);
                            }
                        }
                    }

                    if (isAdded()) {
                        updateNotificationList(unreadNotifications);

                        if (unreadNotifications.isEmpty() && getContext() != null) {
                            Toast.makeText(getContext(), "No new notifications", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch notifications", e);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setupLongPressToDismiss() {
        notificationListView.setOnItemLongClickListener((parent, view, position, id) -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Dismiss Notification")
                    .setMessage("Mark this notification as read?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        notificationAdapter.markNotificationAsRead(position);
                        Toast.makeText(getContext(), "Notification dismissed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    /** Updates the entrant notification list in the adapter.
     * @param notifications unread notifications to display.
     */
    private void updateNotificationList(ArrayList<Notification> notifications) {
        notificationList.clear();
        notificationList.addAll(notifications);
        notificationAdapter.notifyDataSetChanged();
    }
    /** Sets up the RecyclerView and loads organizer data for admin view.
     * @param root inflated admin layout.
     */
    private void setupAdminNotifications(View root) {
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = root.findViewById(R.id.notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty list
        adapter = new AdminNotificationAdapter(new ArrayList<>());

        // Set click listener for organizer items
        adapter.setOnOrganizerClickListener(organizer -> {
            // Navigate to detailed notification page
            navigateToDetailedNotifications(organizer);
        });

        recyclerView.setAdapter(adapter);

        // Load organizer data directly from users collection
        loadOrganizersFromUsers();
    }

    /**
     * Navigates to the detailed notification page for a specific organizer.
     * @param organizer The organizer whose notifications to display
     */
    private void navigateToDetailedNotifications(OrganizerInfo organizer) {
        Log.d(TAG, "Navigating to notifications for organizer: " + organizer.getName());

        // Create a new instance of the detailed notification fragment
        AdminDetailedNotificationFragment detailedFragment = AdminDetailedNotificationFragment.newInstance(
                organizer.getId(),
                organizer.getName(),
                organizer.getEmail()
        );

        // Use manual fragment transaction since this fragment is not in the nav graph
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, detailedFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Loads all organizers directly from the users collection.
     */
    private void loadOrganizersFromUsers() {
        Log.d(TAG, "Loading organizers from users collection...");

        // Get all users and filter for ORGANIZER role
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrganizerInfo> organizerList = new ArrayList<>();

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " total user documents");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String id = document.getId();
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String roleString = document.getString("role");

                            Log.d(TAG, "Processing user - ID: " + id + ", Name: " + name + ", Role: " + roleString);

                            // Check if role is ORGANIZER
                            if (roleString != null && roleString.equals("ORGANIZER")) {
                                if (name != null && email != null) {
                                    organizerList.add(new OrganizerInfo(id, name, email));
                                    Log.d(TAG, "Added organizer: " + name);
                                } else {
                                    Log.w(TAG, "Skipped organizer " + id + " - missing name or email");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing user document", e);
                        }
                    }

                    Log.d(TAG, "Total organizers to display: " + organizerList.size());
                    adapter.updateData(organizerList);

                    // Check if fragment is still attached before showing Toast
                    if (isAdded() && getContext() != null) {
                        if (organizerList.isEmpty()) {
                            Toast.makeText(getContext(), "No organizers found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Loaded " + organizerList.size() + " organizers", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading organizers from users collection", e);
                    // Check if fragment is still attached before showing Toast
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load organizers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Simple data class to hold organizer information.
     */
    public static class OrganizerInfo {
        private String id;
        private String name;
        private String email;

        public OrganizerInfo(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    private void setupOrganizerNotifications(View root) {
        recyclerView = root.findViewById(R.id.organizer_notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        organizerEventAdapter = new OrganizerEventAdapter(event -> {
            OrganizerEventNotificationFragment fragment =
                    OrganizerEventNotificationFragment.newInstance(
                            event.getEventId(),
                            event.getName()
                    );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(organizerEventAdapter);
        loadEventsForOrganizer();
    }

    private void loadEventsForOrganizer() {
        fDatabase = FDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String organizerId = auth.getCurrentUser().getUid();
        fDatabase.queryEventsByCreator(organizerId, new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    organizerEventAdapter.setEvents(events);
                }
            }
            @Override
            public void onFailure(Exception e) {
                if (isAdded())
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
