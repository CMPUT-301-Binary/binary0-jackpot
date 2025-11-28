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
 * Displays entrants who have been invited to an event.
 */
public class InvitedListFragment extends Fragment {
    private Event event;

    public static InvitedListFragment newInstance(Event event) {
        InvitedListFragment fragment = new InvitedListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_invited_list, container, false);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        TextView title = root.findViewById(R.id.event_title);
        RecyclerView recyclerView = root.findViewById(R.id.invited_recycler_view);
        Button backButton = root.findViewById(R.id.back_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (event != null) {
            title.setText(event.getName());
            UserList invited = event.getInvitedList();
            List<User> invitedUsers = invited != null && invited.getUsers() != null
                    ? invited.getUsers() : new ArrayList<>();
            recyclerView.setAdapter(new InvitedListAdapter(invitedUsers));
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return root;
    }

    private static class InvitedListAdapter extends RecyclerView.Adapter<InvitedListAdapter.InvitedViewHolder> {
        private final List<User> invited;

        InvitedListAdapter(List<User> invited) {
            this.invited = invited != null ? invited : new ArrayList<>();
        }

        @NonNull
        @Override
        public InvitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.waiting_list_item, parent, false);
            return new InvitedViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InvitedViewHolder holder, int position) {
            User user = invited.get(position);
            String name = user != null ? user.getName() : null;
            holder.nameView.setText(name != null && !name.isEmpty() ? name : "Unnamed entrant");
            holder.notifyButton.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return invited.size();
        }

        static class InvitedViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            View notifyButton;

            InvitedViewHolder(@NonNull View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.attendee_name);
                notifyButton = itemView.findViewById(R.id.notify_button);
            }
        }
    }
}
