package com.example.jackpot.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
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
import java.util.Calendar;
import java.util.stream.Collectors;

/**
 * A fragment for the home page.
 * This fragment is used to display a list of events.
 * You can filter the events by category, date, or location.
 * You can also click on an event to view its details and join it.
 */
public class HomeFragment extends Fragment {

    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();
    private User currentUser;
    private EventList dataList = new EventList(new ArrayList<>());
    private SearchView searchView;

    // From OpenAI, ChatGPT (GPT-5 Thinking), "ListenerRegistration field for live updates from Firestore (events collection)", 2025-11-07
    private com.google.firebase.firestore.ListenerRegistration eventsReg;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param role The role of the user; this determines what the homepage will look like/what fragment to open.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String role) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("role", role);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Checks the user's role to inflate the correct fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String roleName = getArguments() != null ? getArguments().getString("role") : User.Role.ENTRANT.name();
        User.Role role = User.Role.valueOf(roleName);

        View root;
        int eventItemLayoutResource;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_home_organizer, container, false);
                eventItemLayoutResource = R.layout.event_list_item;
                break;
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_home_admin, container, false);
                eventItemLayoutResource = R.layout.event_list_item;
                break;
            default:
                root = inflater.inflate(R.layout.fragment_home_entrant, container, false);
                eventItemLayoutResource = R.layout.event_list_item;
                break;
        }

        eventList = root.findViewById(R.id.events_list);
        eventAdapter = new EventArrayAdapter(requireActivity(), new ArrayList<>(),
                eventItemLayoutResource, EventArrayAdapter.ViewType.HOME, null);
        eventList.setAdapter(eventAdapter);

        searchView = root.findViewById(R.id.searchView);

        fetchUserAndLoadEvents();
        setupFilterButtons(root);
        setupSearchView();

        return root;
    }

    /**
     * Fetches all events from the database and updates the UI.
     */
    private void loadEvents() {
        fDatabase.getAllEvents(new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> data) {
                if (isAdded()) {
                    dataList.getEvents().clear();
                    dataList.getEvents().addAll(data);
                    updateEventList(dataList.getEvents());
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("HomeFragment", "Failed to load events", e);
            }
        });
    }

    /**
     * Fetches the current user from Firebase and then loads all events.
     * The user object is passed to the adapter to enable role-specific UI elements.
     */
    private void fetchUserAndLoadEvents() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            fDatabase.getUserById(firebaseUser.getUid(), new FDatabase.DataCallback<>() {
                @Override
                public void onSuccess(ArrayList<User> data) {
                    if (isAdded() && !data.isEmpty()) {
                        currentUser = data.get(0);
                        if (eventAdapter != null) {
                            eventAdapter.setCurrentUser(currentUser);
                        }
                    }
                    loadEvents();
                }
                @Override
                public void onFailure(Exception e) {
                    Log.e("HomeFragment", "Error fetching user", e);
                    loadEvents();
                }
            });
        } else {
            loadEvents();
        }
    }

    /**
     * Sets up click listeners for all the filter buttons on the home screen.
     * @param root The root view of the fragment.
     */
    private void setupFilterButtons(View root) {
        ImageButton partyButton = root.findViewById(R.id.party_button);
        ImageButton concertButton = root.findViewById(R.id.concert_button);
        ImageButton charityButton = root.findViewById(R.id.charity_button);
        ImageButton fairButton = root.findViewById(R.id.fair_button);
        Button clearFiltersButton = root.findViewById(R.id.clear_filters_button);

        partyButton.setOnClickListener(v -> filterByCategory("Party"));
        concertButton.setOnClickListener(v -> filterByCategory("Concert"));
        charityButton.setOnClickListener(v -> filterByCategory("Charity"));
        fairButton.setOnClickListener(v -> filterByCategory("Fair"));

        clearFiltersButton.setOnClickListener(v -> {
            updateEventList(dataList.getEvents());
            searchView.setQuery("", false);
            searchView.clearFocus();
        });

        Button dateButton = root.findViewById(R.id.timeButton);
        Button locationButton = root.findViewById(R.id.locationButton);
        Button historyButton = root.findViewById(R.id.historyButton);

        dateButton.setOnClickListener(v -> showDatePickerDialog());
        locationButton.setOnClickListener(v -> showLocationFilterDialog());

        historyButton.setOnClickListener(v -> {
            if (currentUser != null) {
                filterByHistory();
            } else {
                Toast.makeText(getContext(), "not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the search view query listener to filter events as the user types.
     */
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEventsBySearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEventsBySearch(newText);
                return true;
            }
        });
    }

    /**
     * Filters the event list based on a search query.
     * The query is matched against the event name, description, location, and category.
     * @param query The text to search for.
     */
    private void filterEventsBySearch(String query) {
        if (dataList == null || dataList.getEvents() == null) return;

        String lowerCaseQuery = query.toLowerCase();

        ArrayList<Event> filteredList = dataList.getEvents().stream()
                .filter(event -> (event.getName() != null && event.getName().toLowerCase().contains(lowerCaseQuery)) ||
                        (event.getDescription() != null && event.getDescription().toLowerCase().contains(lowerCaseQuery)) ||
                        (event.getLocation() != null && event.getLocation().toLowerCase().contains(lowerCaseQuery)) ||
                        (event.getCategory() != null && event.getCategory().toLowerCase().contains(lowerCaseQuery)))
                .collect(Collectors.toCollection(ArrayList::new));

        updateEventList(filteredList);
    }

    /**
     * Filters the event list by a specific category.
     * @param category The category to filter by.
     */
    private void filterByCategory(String category) {
        if (dataList == null || dataList.getEvents() == null) return;

        ArrayList<Event> filteredList = dataList.getEvents().stream()
                .filter(event -> category.equalsIgnoreCase(event.getCategory()))
                .collect(Collectors.toCollection(ArrayList::new));

        updateEventList(filteredList);
        Toast.makeText(getContext(), "Showing " + category + " events", Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays the standard Android date picker dialog.
     */
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            filterByDate(year1, monthOfYear, dayOfMonth);
        }, year, month, day).show();
    }

    /**
     * Filters the event list to show only events on the selected date.
     * @param year The year to filter by.
     * @param month The month to filter by.
     * @param day The day to filter by.
     */
    private void filterByDate(int year, int month, int day) {
        if (dataList == null || dataList.getEvents() == null) return;

        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event event : dataList.getEvents()) {
            if (event.getDate() != null) {
                Calendar eventCal = Calendar.getInstance();
                eventCal.setTime(event.getDate());
                if (eventCal.get(Calendar.YEAR) == year &&
                    eventCal.get(Calendar.MONTH) == month &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == day) {
                    filteredList.add(event);
                }
            }
        }
        updateEventList(filteredList);

        String dateStr = (month + 1) + "/" + day + "/" + year;
        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No events found for " + dateStr, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Showing events for " + dateStr, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a dialog prompting the user to enter a location to filter by.
     */
    private void showLocationFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter by location");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter location");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String filterValue = input.getText().toString().trim().toLowerCase();
            if (!filterValue.isEmpty()) {
                ArrayList<Event> filteredEvents = new ArrayList<>();
                for (Event event : dataList.getEvents()) {
                    if (event.getLocation() != null && event.getLocation().toLowerCase().contains(filterValue)) {
                        filteredEvents.add(event);
                    }
                }
                updateEventList(filteredEvents);
            } else {
                updateEventList(dataList.getEvents());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Updates the event list adapter with a new list of events.
     * @param events The new list of events to display.
     */
    private void updateEventList(ArrayList<Event> events) {
        if (eventAdapter != null) {
            eventAdapter.clear();
            eventAdapter.addAll(events);
            eventAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Filters the event list to show only events the current user has joined.
     */
    private void filterByHistory(){
        if(dataList == null || dataList.getEvents() == null || currentUser == null){
            return;
        }
        ArrayList<Event> filteredList = new ArrayList<>();
        for (Event event : dataList.getEvents()) {
            if (event.getWaitingList() != null && event.getWaitingList().contains(currentUser)) {
                filteredList.add(event);
            }
        }
        updateEventList(filteredList);
    }
}
