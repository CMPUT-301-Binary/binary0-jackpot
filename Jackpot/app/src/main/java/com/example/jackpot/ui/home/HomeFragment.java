package com.example.jackpot.ui.home;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.jackpot.User;
import com.example.jackpot.Event;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private ListView eventList;
    private EventArrayAdapter eventAdapter;
    private FDatabase fDatabase = FDatabase.getInstance();

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

        // Initialize the event list and adapter
        EventList dataList = new EventList(new ArrayList<>());
        assert root != null;
        eventList = root.findViewById(R.id.events_list);
        eventAdapter = new EventArrayAdapter(requireActivity(), dataList.getEvents(), eventItemLayoutResource);
        eventList.setAdapter(eventAdapter);

        fDatabase.getAllEvents(new FDatabase.DataCallback<Event>() {
            @Override
            public void onSuccess(ArrayList<Event> data) {
                eventAdapter.clear();
                eventAdapter.addAll(data);
            }
            @Override
            public void onFailure(Exception e) {
                // Handle failure
                e.printStackTrace();
            }
        });
        return root;
    }
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//    }
}

