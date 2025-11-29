package com.example.jackpot.ui.events;

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

import com.example.jackpot.Event;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.example.jackpot.UserList;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows entrants who accepted their invitations (joined list).
 */
public class AttendeesListFragment extends Fragment {
    private Event event;

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


        // keep export button visible even if not wired yet

        if (event != null) {
            UserList joined = event.getJoinedList();
            List<User> attendees = joined != null && joined.getUsers() != null
                    ? joined.getUsers() : new ArrayList<>();
            recyclerView.setAdapter(new AttendeesListAdapter(attendees));
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return root;
    }

    private static class AttendeesListAdapter extends RecyclerView.Adapter<AttendeesListAdapter.AttendeeViewHolder> {
        private final List<User> attendees;

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
            //holder.statusView.setText("Confirmed");
           // holder.checkBox.setVisibility(View.GONE);
           // holder.replaceButton.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return attendees.size();
        }

        static class AttendeeViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            TextView statusView;
            View checkBox;
            View replaceButton;

            AttendeeViewHolder(@NonNull View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.attendee_name);

            }
        }
    }
}
