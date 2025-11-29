package com.example.jackpot.ui.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification fragment, which will show the notifications of the user.
 * For ADMIN role, displays all organizers (name and email) from the users collection.
 */
public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";
    private RecyclerView recyclerView;
    private AdminNotificationAdapter adapter;
    private FirebaseFirestore db;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This checks the role of the user, to determine what the page should look like.
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
                break;
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_notification_admin, container, false);
                setupAdminNotifications(root);
                break;
            default:
                root = inflater.inflate(R.layout.fragment_notification_entrant, container, false);
                break;
        }

        return root;
    }

    /**
     * Sets up the RecyclerView and loads organizer data for admin view.
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
}