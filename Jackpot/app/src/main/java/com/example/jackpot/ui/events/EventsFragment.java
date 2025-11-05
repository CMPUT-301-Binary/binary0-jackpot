package com.example.jackpot.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.Event;
import com.example.jackpot.EventArrayAdapter;
import com.example.jackpot.EventList;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
    private ListView eventList;
    private EventArrayAdapter eventAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String roleName = getArguments() != null ? getArguments().getString("role") : "ENTRANT";
        User.Role role = User.Role.valueOf(roleName);

        if (roleName != null) {
            role = User.Role.valueOf(roleName);
        } else {
            // Default to ENTRANT or whatever makes sense
            role = User.Role.ENTRANT;
        }
        role = User.Role.ORGANIZER; // TESTINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG
        // Inflate correct home layout
        View root;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_events_organizer, container, false);
                break;
            default:
                root = inflater.inflate(R.layout.fragment_events_entrant, container, false);
                break;
        }

        return root;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventList dataList = new EventList(new ArrayList<>());

        assert getView() != null;
        eventList = getView().findViewById(R.id.event_list);
        eventAdapter = new EventArrayAdapter(getActivity(), dataList.getEvents());
        eventList.setAdapter(eventAdapter);

        getView().findViewById(R.id.joined_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // joined tab
            }
        });
        getView().findViewById(R.id.wishlist_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // joined tab
            }
        });
        getView().findViewById(R.id.invits_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // joined tap
            }
        });
    }
}

