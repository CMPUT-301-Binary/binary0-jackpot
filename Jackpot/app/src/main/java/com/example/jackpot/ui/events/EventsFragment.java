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
import com.example.jackpot.UserList;
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
        userRole = User.Role.valueOf(roleName);
        Log.d("ROLE_CHECK", "Received role = " + roleName);

        View root;
        int eventItemLayoutResource;

        switch (userRole) {
            case ORGANIZER:
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_events_organizer, container, false);
                eventItemLayoutResource = R.layout.drawlist_item_for_organizer;
                break;
            default:
                root = inflater.inflate(R.layout.fragment_events_entrant, container, false);
                eventItemLayoutResource = R.layout.entrant_event_content;
                break;
        }

        eventList = root.findViewById(userRole == User.Role.ENTRANT ? R.id.entrant_events : R.id.organizer_events);
        eventAdapter = new EventArrayAdapter(requireActivity(),
                displayedEvents.getEvents(),
                eventItemLayoutResource,
                EventArrayAdapter.ViewType.EVENTS,
                null);
        eventList.setAdapter(eventAdapter);

        if (userRole == User.Role.ENTRANT) {
            setupTabs(root);
            getUserAndLoadEvents();
        } else { // ORGANIZER or ADMIN
            setupOrganizerTabs(root);
            getUserAndLoadOrganizerEvents();
            setupOrganizerEventListeners();
        }

        return root;
    }

    private void setupOrganizerEventListeners() {
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

    private void navigateToWaitingList(Event event) {
        WaitingListFragment fragment = WaitingListFragment.newInstance(event);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToCancelList(Event event) {
        CancelListFragment fragment = CancelListFragment.newInstance(event);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupOrganizerTabs(View root) {
        Button myEventsButton = root.findViewById(R.id.my_events_button);
        Button activeButton = root.findViewById(R.id.active_events_button);

        int activeColor = ContextCompat.getColor(requireContext(), R.color.black);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.white);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.black);

        myEventsButton.setBackgroundColor(activeColor);
        myEventsButton.setTextColor(activeTextColor);
        activeButton.setBackgroundColor(inactiveColor);
        activeButton.setTextColor(inactiveTextColor);

        myEventsButton.setOnClickListener(v -> {
            myEventsButton.setBackgroundColor(activeColor);
            myEventsButton.setTextColor(activeTextColor);
            activeButton.setBackgroundColor(inactiveColor);
            activeButton.setTextColor(inactiveTextColor);
            
            // Set the new layout and reset the adapter to force a refresh
            eventAdapter.setLayoutResource(R.layout.drawlist_item_for_organizer);
            eventList.setAdapter(eventAdapter);
            
            loadOrganizerEvents();
        });

        activeButton.setOnClickListener(v -> {
            activeButton.setBackgroundColor(activeColor);
            activeButton.setTextColor(activeTextColor);
            myEventsButton.setBackgroundColor(inactiveColor);
            myEventsButton.setTextColor(inactiveTextColor);

            // Set the new layout and reset the adapter to force a refresh
            eventAdapter.setLayoutResource(R.layout.item_event_organizer);
            eventList.setAdapter(eventAdapter);
            loadActiveOrganizerEvents();
        });
    }

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

    private void loadOrganizerEvents() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null, cannot load organizer events");
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
                        if (event.getDate() != null && event.getDate().getTime() > currentTime) {
                            activeEvents.add(event);
                        }
                    }
                    updateEventList(activeEvents);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventsFragment", "Failed to load active events", e);
            }
        });
    }

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

    private void getUserAndLoadEvents() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getUid() != null) {
            fDatabase.getUserById(firebaseUser.getUid(), new FDatabase.DataCallback<User>() {
                @Override
                public void onSuccess(ArrayList<User> data) {
                    if (isAdded() && !data.isEmpty()) {
                        currentUser = data.get(0);
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

    private void loadEventsForTab() {
        if (currentUser == null) {
            Log.d("EventsFragment", "User null");
            return;
        }

        fDatabase.getAllEvents(new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (isAdded()) {
                    ArrayList<Event> listToDisplay = new ArrayList<>();
                    for (Event event : events) {
                        boolean shouldInclude = false;
                        switch (currentTab) {
                            case JOINED:
                                shouldInclude = event.entrantInList(currentUser.getId(), event.getJoinedList());
                                break;
                            case INVITATIONS:
                                shouldInclude = event.entrantInList(currentUser.getId(), event.getInvitedList());
                                break;
                            case WISHLIST:
                            default:
                                shouldInclude = event.entrantInList(currentUser.getId(), event.getWaitingList());
                                break;
                        }
                        if (shouldInclude) listToDisplay.add(event);
                    }
                    updateEventList(listToDisplay);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventsFragment", "Failed to load events for tab: " + currentTab, e);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEventList(ArrayList<Event> events) {
        eventAdapter.clear();
        if (events != null) {
            eventAdapter.addAll(events);
        }
        if (events == null || events.isEmpty()) {
            Toast.makeText(getContext(), "No events found.", Toast.LENGTH_SHORT).show();
        }
    }
}
