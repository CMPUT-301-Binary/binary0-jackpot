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

/**
 * A fragment that displays events relevant to the current user.
 * This screen is tailored to the user's role and allows them to view events they are
 * associated with, such as events they have joined the waiting list for ('Wishlist').
 * It features a tab-based navigation to switch between different event categories.
 */
public class EventsFragment extends Fragment {
    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();
    private User currentUser;
    private EventList displayedEvents = new EventList(new ArrayList<>());
    private User.Role userRole;

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
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String roleName = getArguments() != null ? getArguments().getString("role") : User.Role.ENTRANT.name();
        userRole = User.Role.valueOf(roleName);
        Log.d("ROLE_CHECK", "Received role = " + roleName);
        Log.d("EventsFragment", "User role enum = " + userRole);

        View root;
        int eventItemLayoutResource;

        switch (userRole) {
            case ORGANIZER:
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_events_organizer, container, false);
                eventItemLayoutResource = R.layout.item_event_organizer;
                break;
            default:
                root = inflater.inflate(R.layout.fragment_events_entrant, container, false);
                eventItemLayoutResource = R.layout.entrant_event_content;
                break;
        }

        if (userRole == User.Role.ENTRANT) {
            eventList = root.findViewById(R.id.entrant_events);
            eventAdapter = new EventArrayAdapter(requireActivity(),
                    displayedEvents.getEvents(),
                    eventItemLayoutResource,
                    EventArrayAdapter.ViewType.EVENTS,
                    null);
            eventList.setAdapter(eventAdapter);
            setupTabs(root);
            getUserAndLoadEvents();
        } else { // ORGANIZER or ADMIN
            eventList = root.findViewById(R.id.organizer_events);
            eventAdapter = new EventArrayAdapter(requireActivity(),
                    displayedEvents.getEvents(),
                    eventItemLayoutResource,
                    EventArrayAdapter.ViewType.EVENTS,
                    null);
            eventList.setAdapter(eventAdapter);
            setupOrganizerTabs(root);
            getUserAndLoadOrganizerEvents();
            setupOrganizerEventListeners();
        }

        return root;
    }

    /**
     * Sets up click listeners for organizer event list items
     */
    private void setupOrganizerEventListeners() {
        // Set up button click listener for waiting list and cancel list buttons
        eventAdapter.setOnButtonClickListener(new EventArrayAdapter.OnButtonClickListener() {
            @Override
            public void onWaitingListClick(Event event) {
                navigateToWaitingList(event);
            }

            @Override
            public void onCancelListClick(Event event) {
                navigateToCancelList(event);
            }
        });
    }

    /**
     * Navigate to the Waiting List fragment
     */
    private void navigateToWaitingList(Event event) {
        WaitingListFragment fragment = WaitingListFragment.newInstance(event);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Navigate to the Cancel List fragment
     */
    private void navigateToCancelList(Event event) {
        CancelListFragment fragment = CancelListFragment.newInstance(event);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Sets up the tabs for organizer view.
     */
    private void setupOrganizerTabs(View root) {
        Button myEventsButton = root.findViewById(R.id.my_events_button);
        Button activeButton = root.findViewById(R.id.active_events_button);
        Button pastButton = root.findViewById(R.id.past_events_button);

        int activeColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.white);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.black);

        // Set initial state - My Events active
        myEventsButton.setBackgroundColor(activeColor);
        myEventsButton.setTextColor(activeTextColor);
        activeButton.setBackgroundColor(inactiveColor);
        activeButton.setTextColor(inactiveTextColor);
        pastButton.setBackgroundColor(inactiveColor);
        pastButton.setTextColor(inactiveTextColor);

        myEventsButton.setOnClickListener(v -> {
            myEventsButton.setBackgroundColor(activeColor);
            myEventsButton.setTextColor(activeTextColor);
            activeButton.setBackgroundColor(inactiveColor);
            activeButton.setTextColor(inactiveTextColor);
            pastButton.setBackgroundColor(inactiveColor);
            pastButton.setTextColor(inactiveTextColor);
            loadOrganizerEvents();
        });

        activeButton.setOnClickListener(v -> {
            activeButton.setBackgroundColor(activeColor);
            activeButton.setTextColor(activeTextColor);
            myEventsButton.setBackgroundColor(inactiveColor);
            myEventsButton.setTextColor(inactiveTextColor);
            pastButton.setBackgroundColor(inactiveColor);
            pastButton.setTextColor(inactiveTextColor);
            loadActiveOrganizerEvents();
        });

        pastButton.setOnClickListener(v -> {
            pastButton.setBackgroundColor(activeColor);
            pastButton.setTextColor(activeTextColor);
            myEventsButton.setBackgroundColor(inactiveColor);
            myEventsButton.setTextColor(inactiveTextColor);
            activeButton.setBackgroundColor(inactiveColor);
            activeButton.setTextColor(inactiveTextColor);
            loadPastOrganizerEvents();
        });
    }

    /**
     * Gets the current user and loads their created events (for organizers).
     */
    private void getUserAndLoadOrganizerEvents() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getUid() != null) {
            fDatabase.getUserById(firebaseUser.getUid(), new FDatabase.DataCallback<User>() {
                @Override
                public void onSuccess(ArrayList<User> data) {
                    if (isAdded() && !data.isEmpty()) {
                        currentUser = data.get(0);
                        eventAdapter.setCurrentUser(currentUser);
                        loadOrganizerEvents();
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
     * Loads all events created by the organizer.
     */
    private void loadOrganizerEvents() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null");
            return;
        }

        fDatabase.queryEventsByCreator(currentUser.getId(), new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    updateEventList(events);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventsFragment", "Failed to load organizer events", e);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading your events.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Loads active events created by the organizer (events in the future).
     */
    private void loadActiveOrganizerEvents() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null");
            return;
        }

        fDatabase.queryEventsByCreator(currentUser.getId(), new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    ArrayList<Event> activeEvents = new ArrayList<>();
                    long currentTime = System.currentTimeMillis();

                    for (Event event : events) {
                        // Check if event is in the future using the date field
                        if (event.getDate() != null &&
                                event.getDate().getTime() > currentTime) {
                            activeEvents.add(event);
                        }
                    }
                    updateEventList(activeEvents);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventsFragment", "Failed to load active events", e);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading active events.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Loads past events created by the organizer.
     */
    private void loadPastOrganizerEvents() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null");
            return;
        }

        fDatabase.queryEventsByCreator(currentUser.getId(), new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    ArrayList<Event> pastEvents = new ArrayList<>();
                    long currentTime = System.currentTimeMillis();

                    for (Event event : events) {
                        // Check if event is in the past using the date field
                        if (event.getDate() != null &&
                                event.getDate().getTime() <= currentTime) {
                            pastEvents.add(event);
                        }
                    }
                    updateEventList(pastEvents);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventsFragment", "Failed to load past events", e);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading past events.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sets up the tabs for the event list (entrant view).
     */
    private void setupTabs(View root) {
        Button joinedButton = root.findViewById(R.id.joined_events_button);
        Button wishlistButton = root.findViewById(R.id.wishlist_events_button);
        Button invitsButton = root.findViewById(R.id.invits_events_button);

        int activeColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.white);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.black);

        wishlistButton.setBackgroundColor(activeColor);
        wishlistButton.setTextColor(activeTextColor);
        joinedButton.setBackgroundColor(inactiveColor);
        joinedButton.setTextColor(inactiveTextColor);
        invitsButton.setBackgroundColor(inactiveColor);
        invitsButton.setTextColor(inactiveTextColor);

        joinedButton.setOnClickListener(v -> {
            currentTab = EventTab.JOINED;
            joinedButton.setBackgroundColor(activeColor);
            joinedButton.setTextColor(activeTextColor);
            wishlistButton.setBackgroundColor(inactiveColor);
            wishlistButton.setTextColor(inactiveTextColor);
            invitsButton.setBackgroundColor(inactiveColor);
            invitsButton.setTextColor(inactiveTextColor);
            loadEventsForTab();
        });

        wishlistButton.setOnClickListener(v -> {
            currentTab = EventTab.WISHLIST;
            wishlistButton.setBackgroundColor(activeColor);
            wishlistButton.setTextColor(activeTextColor);
            joinedButton.setBackgroundColor(inactiveColor);
            joinedButton.setTextColor(inactiveTextColor);
            invitsButton.setBackgroundColor(inactiveColor);
            invitsButton.setTextColor(inactiveTextColor);
            loadEventsForTab();
        });

        invitsButton.setOnClickListener(v -> {
            currentTab = EventTab.INVITATIONS;
            invitsButton.setBackgroundColor(activeColor);
            invitsButton.setTextColor(activeTextColor);
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
                        Log.d("EventsFragment", "Current user loaded: " + currentUser.getName());
                        eventAdapter.setCurrentUser(currentUser);
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
     * Loads the events for the current tab (entrant view).
     */
    private void loadEventsForTab() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null in loadEventsForTab");
            return;
        }

        Log.d("EventsFragment", "Loading events for tab: " + currentTab);

        FDatabase.DataCallback<Event> callback = new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    Log.d("EventsFragment", "Total events fetched: " + events.size());
                    Log.d("EventsFragment", "Current user ID: " + currentUser.getId());

                    ArrayList<Event> filteredEvents = new ArrayList<>();

                    for (Event event : events) {
                        Log.d("EventsFragment", "Checking event: " + event.getName());

                        if (event.getWaitingList() != null) {
                            Log.d("EventsFragment", "Waiting list size: " + event.getWaitingList().size());

                            if (event.hasEntrant(currentUser.getId())) {
                                Log.d("EventsFragment", "User found in event: " + event.getName());
                                filteredEvents.add(event);
                            } else {
                                Log.d("EventsFragment", "User NOT in event: " + event.getName());
                            }
                        } else {
                            Log.d("EventsFragment", "Event has null waiting list: " + event.getName());
                        }
                    }

                    Log.d("EventsFragment", "Filtered events: " + filteredEvents.size());
                    updateEventList(filteredEvents);
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
                fDatabase.getAllEvents(callback);
                break;
            case INVITATIONS:
                Toast.makeText(getContext(), "TODO: Invitations Events", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Updates the event list with the given events.
     */
    private void updateEventList(ArrayList<Event> events) {
        Log.d("EventsFragment", "updateEventList called with " + (events != null ? events.size() : 0) + " events");

        ArrayList<Event> adapterList = displayedEvents.getEvents();
        adapterList.clear();

        if (events != null && !events.isEmpty()) {
            adapterList.addAll(events);
            Log.d("EventsFragment", "Added events to adapter list. New size: " + adapterList.size());
        }

        // Force adapter refresh
        if (eventAdapter != null) {
            Log.d("EventsFragment", "Calling notifyDataSetChanged on adapter");
            eventAdapter.notifyDataSetChanged();
        } else {
            Log.e("EventsFragment", "eventAdapter is NULL!");
        }

        // Log ListView state
        if (eventList != null) {
            Log.d("EventsFragment", "ListView adapter count: " + eventList.getAdapter().getCount());
            Log.d("EventsFragment", "ListView visibility: " + eventList.getVisibility());
        } else {
            Log.e("EventsFragment", "eventList ListView is NULL!");
        }

        if (events == null || events.isEmpty()) {
            Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
        }
    }
}
