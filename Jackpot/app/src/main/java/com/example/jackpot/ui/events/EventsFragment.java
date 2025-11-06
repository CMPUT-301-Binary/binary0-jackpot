package com.example.jackpot.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.EventArrayAdapter;
import com.example.jackpot.EventList;
import com.example.jackpot.FDatabase;
import com.example.jackpot.R;
import com.example.jackpot.User;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();
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
        EventList dataList = new EventList(new ArrayList<>());
        assert root != null;
        eventList = root.findViewById(R.id.entrant_events);
        eventAdapter = new EventArrayAdapter(requireActivity(), dataList.getEvents(), eventItemLayoutResource);
        eventList.setAdapter(eventAdapter);

        root.findViewById(R.id.joined_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // joined tab
            }
        });
        root.findViewById(R.id.wishlist_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // joined tab
            }
        });
        root.findViewById(R.id.invits_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // joined tap
            }
        });

        return root;
    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//    }
}

