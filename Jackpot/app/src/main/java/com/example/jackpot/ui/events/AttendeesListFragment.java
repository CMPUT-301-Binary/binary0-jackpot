package com.example.jackpot.ui.events;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.CSVExporter;
import com.example.jackpot.Event;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.example.jackpot.UserList;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows entrants who accepted their invitations (joined list).
 *
 * Responsibilities:
 *  - Inflate the confirmed attendees layout.
 *  - Read the passed Event argument and extract its joined list.
 *  - Render attendees in a RecyclerView.
 *  - Provide back navigation and CSV export of the attendees list.
 */
public class AttendeesListFragment extends Fragment {
    private Event event;
    private List<User> attendees;

    /**
     * Factory method to create a fragment instance with the given event.
     * @param event Event whose attendees should be displayed.
     * @return configured AttendeesListFragment with arguments set.
     */
    public static AttendeesListFragment newInstance(Event event) {
        AttendeesListFragment fragment = new AttendeesListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_confirmed_list, container, false);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        RecyclerView recyclerView = root.findViewById(R.id.attendees_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Button backButton = root.findViewById(R.id.back_button);
        Button exportButton = root.findViewById(R.id.export_csv_button);

        if (event != null) {
            UserList joined = event.getJoinedList();
            attendees = joined != null && joined.getUsers() != null
                    ? joined.getUsers() : new ArrayList<>();
            recyclerView.setAdapter(new AttendeesListAdapter(attendees));
        } else {
            attendees = new ArrayList<>();
        }

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Export CSV functionality
        exportButton.setOnClickListener(v -> {
            if (event != null && attendees != null && !attendees.isEmpty()) {
                Uri csvUri = CSVExporter.exportAttendeesList(requireContext(), event, attendees);
                if (csvUri != null) {
                    // Optionally open share dialog
                    CSVExporter.shareCSVFile(requireContext(), csvUri);
                }
            } else {
                android.widget.Toast.makeText(requireContext(),
                        "No attendees to export",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private static class AttendeesListAdapter extends RecyclerView.Adapter<AttendeesListAdapter.AttendeeViewHolder> {
        private final List<User> attendees;

        /**
         * Adapter for rendering attendees in the confirmed list.
         * @param attendees the list of attendees to render (null-safe).
         */
        AttendeesListAdapter(List<User> attendees) {
            this.attendees = attendees != null ? attendees : new ArrayList<>();
        }

        @NonNull
        @Override
        public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.confirmed_list_item, parent, false);
            return new AttendeeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
            User user = attendees.get(position);
            String name = user != null ? user.getName() : null;
            holder.nameView.setText(name != null && !name.isEmpty() ? name : "Unnamed entrant");
        }

        @Override
        public int getItemCount() {
            return attendees.size();
        }

        static class AttendeeViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;

            AttendeeViewHolder(@NonNull View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.attendee_name);
            }
        }
    }
}
