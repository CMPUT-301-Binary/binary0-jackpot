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

public class HomeFragment extends Fragment {

    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();
    private User currentUser;
    private EventList dataList = new EventList(new ArrayList<>());
    private SearchView searchView;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String role) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("role", role);
        fragment.setArguments(args);
        return fragment;
    }

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

    private void filterByCategory(String category) {
        if (dataList == null || dataList.getEvents() == null) return;

        ArrayList<Event> filteredList = dataList.getEvents().stream()
                .filter(event -> category.equalsIgnoreCase(event.getCategory()))
                .collect(Collectors.toCollection(ArrayList::new));

        updateEventList(filteredList);
        Toast.makeText(getContext(), "Showing " + category + " events", Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            filterByDate(year1, monthOfYear, dayOfMonth);
        }, year, month, day).show();
    }

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

    private void filterByHistory() {
        if (currentUser == null) return;

        ArrayList<Event> filteredList = dataList.getEvents().stream()
                .filter(event -> event.getWaitingList() != null && event.getWaitingList().contains(currentUser))
                .collect(Collectors.toCollection(ArrayList::new));

        updateEventList(filteredList);
        Toast.makeText(getContext(), "Showing events you're in", Toast.LENGTH_SHORT).show();
    }

    private void updateEventList(ArrayList<Event> newList) {
        if (eventAdapter != null) {
            eventAdapter.clear();
            eventAdapter.addAll(newList);
            eventAdapter.notifyDataSetChanged();
        }
    }
}
