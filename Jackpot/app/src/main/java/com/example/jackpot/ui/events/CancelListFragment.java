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
import java.util.HashMap;
import java.util.List;

/**
 * Displays the cancelled entrant list for a given event and lets organizers view/export it.
 */
public class CancelListFragment extends Fragment {
    private Event event;
    private RecyclerView recyclerView;
    private TextView eventTitle;
    private Button backButton;
    private CancelledAdapter adapter;

    /**
     * Factory to create the fragment with an event argument.
     * @param event Event whose cancelled list will be shown.
     * @return configured fragment instance.
     */
    public static CancelListFragment newInstance(Event event) {
        CancelListFragment fragment = new CancelListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflate the UI, load the cancelled list, and wire navigation/export actions.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
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

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set event details
        if (event != null) {
            eventTitle.setText(event.getName());
            ArrayList<User> cancelledUsers = extractUsers(event.getCancelledList());
            adapter = new CancelledAdapter(cancelledUsers);
            recyclerView.setAdapter(adapter);
        }

        // Back button
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return root;
    }

    /**
     * Converts a UserList (possibly containing map objects from Firestore) into concrete Users.
     * @param list raw list pulled from Firestore.
     * @return list of concrete User models.
     */
    private ArrayList<User> extractUsers(UserList list) {
        ArrayList<User> users = new ArrayList<>();
        if (list == null || list.getUsers() == null) {
            return users;
        }

        for (Object obj : list.getUsers()) {
            if (obj instanceof User) {
                users.add((User) obj);
            } else if (obj instanceof HashMap) {
                HashMap<String, Object> map = (HashMap<String, Object>) obj;
                User user = new User();
                user.setId((String) map.get("id"));
                user.setName((String) map.get("name"));
                user.setEmail((String) map.get("email"));
                users.add(user);
            }
        }
        return users;
    }

    private static class CancelledAdapter extends RecyclerView.Adapter<CancelledAdapter.CancelledViewHolder> {
        private final List<User> cancelled;

        CancelledAdapter(List<User> cancelled) {
            this.cancelled = cancelled != null ? cancelled : new ArrayList<>();
        }

        @NonNull
        @Override
        public CancelledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cancelled_user, parent, false);
            return new CancelledViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CancelledViewHolder holder, int position) {
            User user = cancelled.get(position);
            holder.nameView.setText(user != null && user.getName() != null ? user.getName() : "Unnamed entrant");
            holder.emailView.setText(user != null && user.getEmail() != null ? user.getEmail() : "No email");
        }

        @Override
        public int getItemCount() {
            return cancelled.size();
        }

        static class CancelledViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;
            TextView emailView;

            CancelledViewHolder(@NonNull View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.cancelled_name);
                emailView = itemView.findViewById(R.id.cancelled_email);
            }
        }
    }
}
