package com.example.jackpot.ui.events;

import android.os.Bundle;
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

import com.example.jackpot.Event;
import com.example.jackpot.FDatabase;
import com.example.jackpot.R;

public class CancelListFragment extends Fragment {
    private Event event;
    private RecyclerView recyclerView;
    private TextView eventTitle;
    private ImageView eventImage;
    private Button backButton;
    private Button notifyAllButton;

    public static CancelListFragment newInstance(Event event) {
        CancelListFragment fragment = new CancelListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cancel_list, container, false);

        // Get event from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        // Initialize views
        eventTitle = root.findViewById(R.id.event_title);
        recyclerView = root.findViewById(R.id.cancel_list_recycler_view);
        backButton = root.findViewById(R.id.back_button);
        notifyAllButton = root.findViewById(R.id.notify_all_button);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set event details
        if (event != null) {
            eventTitle.setText(event.getName());
            // Load event image if you have an image loader
            // Glide.with(this).load(event.getImageUrl()).into(eventImage);

            // TODO: Setup RecyclerView adapter with canceled entrants
            // You'll need to create an adapter for displaying entrants
        }

        // Back button
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Notify all button
        notifyAllButton.setOnClickListener(v -> {
            // TODO: Implement notification logic
            Toast.makeText(getContext(), "Notifying all canceled members", Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}