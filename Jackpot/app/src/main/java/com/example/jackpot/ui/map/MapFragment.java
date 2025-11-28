package com.example.jackpot.ui.map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jackpot.Event;
import com.example.jackpot.EventList;
import com.example.jackpot.FDatabase;
import com.example.jackpot.R;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

/**
 * Map fragment that displays the list of events created by the current organizer.
 * Each event can be clicked to navigate to a detailed map view showing user locations.
 */
public class MapFragment extends Fragment implements OrganizerEventAdapter.OnEventClickListener {

    private static final String TAG = "MapFragment";

    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private FDatabase fDatabase;
    private FirebaseAuth auth;
    private EventList eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map_organizer, container, false);

        // Initialize Firebase
        fDatabase = FDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize event list
        eventList = new EventList();

        // Setup RecyclerView
        recyclerView = root.findViewById(R.id.event_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new OrganizerEventAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load events
        loadOrganizerEvents();

        return root;
    }

    /**
     * Loads events created by the current organizer from Firestore.
     * Uses FDatabase method to match EventsFragment implementation.
     */
    private void loadOrganizerEvents() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot load events: user not logged in");
            return;
        }

        String organizerId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading events for organizer: " + organizerId);

        fDatabase.queryEventsByCreator(organizerId, new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (!isAdded()) {
                    return;
                }

                eventList.clearEvents();

                for (Event event : events) {
                    try {
                        // Verify the event has the required fields
                        if (event.getEventId() == null || event.getEventId().isEmpty()) {
                            Log.w(TAG, "Event missing ID, skipping: " + event.getName());
                            continue;
                        }

                        eventList.addEvent(event);
                        Log.d(TAG, "Loaded event: " + event.getName() +
                                " (ID: " + event.getEventId() +
                                ", Capacity: " + event.getCapacity() +
                                ", Waiting: " + (event.getWaitingList() != null ?
                                event.getWaitingList().size() : 0) + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing event: " + event.getName(), e);
                    }
                }

                // Update adapter with loaded events
                adapter.setEvents(eventList.getEvents());

                if (eventList.countEvents() == 0) {
                    Toast.makeText(getContext(),
                            "No events found. Create your first event!",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No events found for organizer");
                } else {
                    Log.d(TAG, "Successfully loaded " + eventList.countEvents() + " events");
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) {
                    return;
                }

                Log.e(TAG, "Error loading events from Firestore", e);
                Toast.makeText(getContext(),
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Handles click events on event items.
     * Navigates to a detailed map view showing user locations for this event.
     *
     * @param event The event that was clicked.
     */
    @Override
    public void onEventClick(Event event) {
        if (event == null) {
            Log.e(TAG, "Clicked event is null");
            return;
        }

        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            Log.e(TAG, "Event ID is null or empty");
            Toast.makeText(getContext(), "Cannot open map: invalid event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Event clicked: " + event.getName() + " (ID: " + event.getEventId() + ")");

        try {
            // Create bundle with event data
            Bundle bundle = new Bundle();
            bundle.putString("EVENT_ID", event.getEventId());
            bundle.putString("EVENT_NAME", event.getName());

            // Get NavController and navigate using Navigation Component
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_map_detail, bundle);

            Log.d(TAG, "Navigated to MapDetailFragment for event: " + event.getName());

        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refresh the event list when the fragment becomes visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed, refreshing event list");
        loadOrganizerEvents();
    }
}