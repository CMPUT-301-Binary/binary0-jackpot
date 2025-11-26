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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.jackpot.Entrant;
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

    /**
     * Required empty public constructor
     */
    public EventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param role The role of the user.
     * @return A new instance of fragment EventsFragment.
     */
    public static EventsFragment newInstance(String role) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString("role", role);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will check the user's role to ensure the correct layout is inflated.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String roleName = getArguments() != null ? getArguments().getString("role") : User.Role.ENTRANT.name();
        User.Role role = User.Role.valueOf(roleName);
        Log.d("ROLE_CHECK", "Received role = " + roleName);
        View root;
        int eventItemLayoutResource;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_events_organizer, container, false);
                eventItemLayoutResource = R.layout.item_event;
                break;
            default:
                root = inflater.inflate(R.layout.fragment_events_entrant, container, false);
                eventItemLayoutResource = R.layout.entrant_event_content;
                break;
        }
        assert root != null;
        if (role == User.Role.ENTRANT) {
            eventList = root.findViewById(R.id.entrant_events);
            eventAdapter = new EventArrayAdapter(requireActivity(),
                    displayedEvents.getEvents(),
                    eventItemLayoutResource,
                    EventArrayAdapter.ViewType.EVENTS,
                    null);
            eventList.setAdapter(eventAdapter);

            setupTabs(root);
            getUserAndLoadEvents();
        }
        return root;
    }

    /**
     * Sets up the tabs for the event list.
     * @param root The root view of the fragment.
     */
    private void setupTabs(View root) {
        Button joinedButton = root.findViewById(R.id.joined_events_button);
        Button wishlistButton = root.findViewById(R.id.wishlist_events_button);
        Button invitsButton = root.findViewById(R.id.invits_events_button);

        // Define colours for the buttons
        int activeColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.white);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.black);

        // set initial state
        wishlistButton.setBackgroundColor(activeColor);
        wishlistButton.setTextColor(activeTextColor);
        joinedButton.setBackgroundColor(inactiveColor);
        joinedButton.setTextColor(inactiveTextColor);
        invitsButton.setBackgroundColor(inactiveColor);
        invitsButton.setTextColor(inactiveTextColor);

        joinedButton.setOnClickListener(v -> {
            currentTab = EventTab.JOINED;
            // Set active state for Joined button
            joinedButton.setBackgroundColor(activeColor);
            joinedButton.setTextColor(activeTextColor);
            // Set inactive state for other buttons
            wishlistButton.setBackgroundColor(inactiveColor);
            wishlistButton.setTextColor(inactiveTextColor);
            invitsButton.setBackgroundColor(inactiveColor);
            invitsButton.setTextColor(inactiveTextColor);
            loadEventsForTab();
        });
        wishlistButton.setOnClickListener(v -> {
            currentTab = EventTab.WISHLIST;
            // Set active state for Wishlist button
            wishlistButton.setBackgroundColor(activeColor);
            wishlistButton.setTextColor(activeTextColor);
            // Set inactive state for other buttons
            joinedButton.setBackgroundColor(inactiveColor);
            joinedButton.setTextColor(inactiveTextColor);
            invitsButton.setBackgroundColor(inactiveColor);
            invitsButton.setTextColor(inactiveTextColor);
            loadEventsForTab();
        });
        invitsButton.setOnClickListener(v -> {
            currentTab = EventTab.INVITATIONS;
            // Set active state for Invitations button
            invitsButton.setBackgroundColor(activeColor);
            invitsButton.setTextColor(activeTextColor);
            // Set inactive state for other buttons
            wishlistButton.setBackgroundColor(inactiveColor);
            wishlistButton.setTextColor(inactiveTextColor);
            joinedButton.setBackgroundColor(inactiveColor);
            joinedButton.setTextColor(inactiveTextColor);
            loadEventsForTab();
        });
    }

    /**
     * Gets the current user and loads their events. Loads the events from firebase.
     */
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

    /**
     * Loads the events for the current tab.
     */
    private void loadEventsForTab() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null");
            return;
        }

        FDatabase.DataCallback<Event> callback = new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    ArrayList<Event> wishlistEvents = new ArrayList<>();
                    Entrant check = new Entrant(
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
                    for (Event event : events) {
                        if (event.getWaitingList() != null && event.hasEntrant(check)) {
                            wishlistEvents.add(event);
                        }
                    }
                    updateEventList(wishlistEvents);
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
//                fDatabase.queryEventsWithArrayContains("waitingList", currentUser, callback);
                fDatabase.getAllEvents(callback);

                break;
            case INVITATIONS:
                Toast.makeText(getContext(), "TODO: Invitations Events", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Updates the event list with the given events.
     * @param events The events to update the list with.
     */
    private void updateEventList(ArrayList<Event> events) {
        ArrayList<Event> adapterList = displayedEvents.getEvents();
        adapterList.clear();
        if (events != null) {
            adapterList.addAll(events);
        }
        eventAdapter.notifyDataSetChanged();
        if (events.isEmpty()) {
            Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
        }
    }
}
