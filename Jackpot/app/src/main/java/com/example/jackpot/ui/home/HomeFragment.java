package com.example.jackpot.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.EventArrayAdapter;
import com.example.jackpot.EventList;
import com.example.jackpot.FDatabase;
import com.example.jackpot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.jackpot.User;
import com.example.jackpot.Event;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();
    private User currentUser;
    private EventList dataList = new EventList(new ArrayList<>());


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

        // Inflate the correct layout for this fragment
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

        // Initialize the event list and adapter immediately.
        assert root != null;
        eventList = root.findViewById(R.id.events_list);
        eventAdapter = new EventArrayAdapter(requireActivity(), new ArrayList<>(), eventItemLayoutResource, null);
        eventList.setAdapter(eventAdapter);

        fetchUserAndLoadEvents();
        // Fetch the current user and update the adapter when done.
        setupFilterButtons(root);

        return root;
    }

    private void loadEvents() {
        fDatabase.getAllEvents(new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> data) {
                if (eventAdapter != null) {
                    dataList.getEvents().clear();
                    dataList.getEvents().addAll(data);
                    updateEventList(dataList.getEvents());
                }
            }
            @Override
            public void onFailure(Exception e) {
                // Handle failure
                e.printStackTrace();
            }
        });
    }
    private void fetchUserAndLoadEvents() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            fDatabase.getUserById(firebaseUser.getUid(), new FDatabase.DataCallback<>() {
                @Override
                public void onSuccess(ArrayList<User> data) {
                    if (!data.isEmpty()) {
                        Log.d("HomeFragment", "User data: " + data.get(0));
                        currentUser = data.get(0);
                        if (eventAdapter != null) {
                            eventAdapter.setCurrentUser(currentUser);
                        }
                    } else {
                        Log.d("HomeFragment", "No user data found.");
                    }
                    loadEvents();
                }
                @Override
                public void onFailure(Exception e) {
                    // Handle user not found or other errors
                    Log.d("HomeFragment", "Error fetching user: " + e.getMessage());
                }
            });
        } else {
            Log.d("HomeFragment", "No user logged in.");

        }
    }
    private void setupFilterButtons(View root) {
        Button timeButton = root.findViewById(R.id.timeButton);
        Button locationButton = root.findViewById(R.id.locationButton);
        Button historyButton = root.findViewById(R.id.historyButton);

        timeButton.setOnClickListener(v -> {showFilterDialog("time");});
        locationButton.setOnClickListener(v -> {showFilterDialog("location");});

        historyButton.setOnClickListener(v -> {
            if (currentUser != null) {
                filterByHistory();
            } else {
                Toast.makeText(getContext(), "not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showFilterDialog(String filterType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter by " + filterType);

        final EditText input = new EditText(requireContext());
        input.setHint("Enter " + filterType);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String filterValue = input.getText().toString().trim().toLowerCase();
            if (!filterValue.isEmpty()) {
                ArrayList<Event> filteredEvents = new ArrayList<>();
                for (Event event : dataList.getEvents()) {
                    if (filterType.equals("location") &&
                            event.getLocation() != null &&
                            event.getLocation().toLowerCase().contains(filterValue)) {
                        filteredEvents.add(event);
                    }
                    else if (filterType.equals("time") &&
                            event.getDate() != null &&
                            event.getDate().toString().toLowerCase().contains(filterValue)) {
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
        }
    }

}
