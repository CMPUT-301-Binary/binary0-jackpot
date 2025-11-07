package com.example.jackpot.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.Event;
import com.example.jackpot.EventArrayAdapter;
import com.example.jackpot.EventList;
import com.example.jackpot.FDatabase;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();
    private User currentUser;
    private EventList displayedEvents = new EventList(new ArrayList<>());
    private enum EventTab {
        JOINED, WISHLIST, INVITATIONS
    }
    private EventTab currentTab = EventTab.WISHLIST;
    public EventsFragment() {
        // Required empty public constructor
    }
    public static EventsFragment newInstance(String role) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString("role", role);
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String roleName = getArguments() != null ? getArguments().getString("role") : User.Role.ENTRANT.name();
        User.Role role = User.Role.valueOf(roleName);

//        role = User.Role.ORGANIZER; // TESTINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG
        // Inflate correct home layout
        View root;
        int eventItemLayoutResource;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_events_organizer, container, false);
                eventItemLayoutResource = R.layout.entrant_event_content;
                break;
            default:
                root = inflater.inflate(R.layout.fragment_events_entrant, container, false);
                eventItemLayoutResource = R.layout.entrant_event_content;
                break;
        }
        assert root != null;
        eventList = root.findViewById(R.id.entrant_events);
        eventAdapter = new EventArrayAdapter(requireActivity(), displayedEvents.getEvents(), eventItemLayoutResource, null);
        eventList.setAdapter(eventAdapter);

        setupTabs(root);
        getUserAndLoadEvents();
        return root;
    }

    private void setupTabs(View root) {
        Button joinedButton = root.findViewById(R.id.joined_events_button);
        Button wishlistButton = root.findViewById(R.id.wishlist_events_button);
        Button invitsButton = root.findViewById(R.id.invits_events_button);

        joinedButton.setOnClickListener(v -> {
            currentTab = EventTab.JOINED;
            loadEventsForTab();
        });
        wishlistButton.setOnClickListener(v -> {
            currentTab = EventTab.WISHLIST;
            loadEventsForTab();
        });
        invitsButton.setOnClickListener(v -> {
            currentTab = EventTab.INVITATIONS;
            loadEventsForTab();
        });
    }
    private void getUserAndLoadEvents() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getUid() != null) {
            fDatabase.getUserById(firebaseUser.getUid(), new FDatabase.DataCallback<User>() {
                @Override
                public void onSuccess(ArrayList<User> data) {
                    if (isAdded() && !data.isEmpty()) {
                        currentUser = data.get(0);
                        eventAdapter.setCurrentUser(currentUser);
                        // Once user is fetched, load events for the default tab
                        loadEventsForTab();
                    } else {
                        Log.d("EventsFragment", "User not found in database.");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("EventsFragment", "Failed to fetch user.", e);
                }
            });
        } else {
            Log.d("EventsFragment", "No Firebase user logged in.");
        }
    }
    private void loadEventsForTab() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null");
            return;
        }

        FDatabase.DataCallback<Event> callback = new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    updateEventList(events);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventsFragment", "Failed to load events for tab: " + currentTab, e);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        switch (currentTab) {
            case JOINED:
                Toast.makeText(getContext(), "TODO: Joined Events", Toast.LENGTH_SHORT).show();
                break;
            case WISHLIST:
                fDatabase.queryEventsWithArrayContains("waitingList", currentUser, callback);
                break;
            case INVITATIONS:
                Toast.makeText(getContext(), "TODO: Invitations Events", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void updateEventList(ArrayList<Event> events) {
        displayedEvents.setEvents(events);
        eventAdapter.notifyDataSetChanged();
        if (events.isEmpty()) {
            Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
        }
    }
}
