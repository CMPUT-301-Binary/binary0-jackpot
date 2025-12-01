package com.example.jackpot.ui.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jackpot.Event;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Displays the waiting list for an event so organizers can view and notify entrants.
 */
public class WaitingListFragment extends Fragment {
    private Event event;
    private RecyclerView recyclerView;
    private TextView eventTitle;
    private ImageView eventImage;
    private Button backButton;
    private Button notifyAllButton;
    private UserArrayAdapter adapter;

    /**
     * Factory to create the fragment with the target event bundled.
     * @param event Event whose waiting list will be shown.
     * @return configured fragment instance.
     */
    public static WaitingListFragment newInstance(Event event) {
        WaitingListFragment fragment = new WaitingListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflate UI, hydrate waiting list data, and wire navigation/notify actions.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        // Get event from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        // Initialize views
        eventTitle = root.findViewById(R.id.event_title);
//        eventImage = root.findViewById(R.id.event_image);
        recyclerView = root.findViewById(R.id.waiting_list_recycler_view);
        backButton = root.findViewById(R.id.back_button);
        notifyAllButton = root.findViewById(R.id.notify_all_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set event details and populate the list
        if (event != null) {
            eventTitle.setText(event.getName());

//            if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
//                Glide.with(this).load(event.getPosterUri()).into(eventImage);
//            }

            // Manually convert Firestore's list of HashMaps into a list of User objects
            ArrayList<User> userList = new ArrayList<>();
            if (event.getWaitingList() != null && event.getWaitingList().getUsers() != null) {
                for (Object obj : event.getWaitingList().getUsers()) {
                    if (obj instanceof HashMap) {
                        try {
                            HashMap<String, Object> map = (HashMap<String, Object>) obj;
                            User user = new User();
                            user.setId((String) map.get("id"));
                            user.setName((String) map.get("name"));
                            user.setEmail((String) map.get("email"));
                            // Add any other fields you need for the User object here
                            userList.add(user);
                        } catch (Exception e) {
                            Log.e("WaitingListFragment", "Failed to convert HashMap to User", e);
                        }
                    } else if (obj instanceof User) {
                        userList.add((User) obj);
                    }
                }
            }

            // Now pass the correctly typed list to the adapter
            adapter = new UserArrayAdapter(requireContext(), userList);
            recyclerView.setAdapter(adapter);

        } else {
            Toast.makeText(getContext(), "Event data is missing.", Toast.LENGTH_SHORT).show();
        }

        // Back button
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Notify all button
        notifyAllButton.setOnClickListener(v -> {
            // TODO: Implement notification logic
            Toast.makeText(getContext(), "Notifying all waiting list members", Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}
