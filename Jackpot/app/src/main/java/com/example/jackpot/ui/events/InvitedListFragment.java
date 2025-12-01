package com.example.jackpot.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.jackpot.User;
import com.example.jackpot.UserList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Displays entrants who have been invited to an event.
 */
public class InvitedListFragment extends Fragment {
    private Event event;
    private InvitedManagementAdapter adapter;

    /**
     * Factory to build a fragment with the event argument set.
     * @param event Event whose invited users are managed here.
     * @return configured InvitedListFragment.
     */
    public static InvitedListFragment newInstance(Event event) {
        InvitedListFragment fragment = new InvitedListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * Inflate UI, hydrate invited/cancelled lists, and wire actions (select-all, replace invites, back).
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
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
        Button selectAllButton = root.findViewById(R.id.button_select_all);
        Button cancelButton = root.findViewById(R.id.button_replace_invites);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (event != null) {
            title.setText(event.getName());
            UserList invited = event.getInvitedList();
            ArrayList<User> invitedUsers = extractUsers(invited);
            if (invited != null) {
                invited.setUsers(invitedUsers);
            }
            if (event.getCancelledList() != null) {
                event.getCancelledList().setUsers(extractUsers(event.getCancelledList()));
            }
            adapter = new InvitedManagementAdapter(invitedUsers);
            recyclerView.setAdapter(adapter);
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        selectAllButton.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.toggleSelectAll();
            }
        });

        cancelButton.setOnClickListener(v -> replaceSelectedInvites());

        return root;
    }

    /**
     * Moves selected invitees to cancelled and backfills with draws from waiting list.
     */
    private void replaceSelectedInvites() {
        if (adapter == null || event == null) {
            return;
        }
        ArrayList<User> selected = adapter.getSelectedUsers();
        if (selected.isEmpty()) {
            Toast.makeText(getContext(), "No invitees selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.getInvitedList() == null) {
            event.setInvitedList(new UserList(event.getCapacity()));
        }
        if (event.getCancelledList() == null) {
            event.setCancelledList(new UserList(event.getCapacity()));
        }

        for (User user : selected) {
            removeById(event.getInvitedList(), user.getId());
            addIfMissing(event.getCancelledList(), user);
        }

        ArrayList<User> replacements = event.drawFromWaiting(selected.size());

        FDatabase.getInstance().updateEvent(event);
        adapter.removeUsers(selected);
        adapter.addUsers(replacements);
        Toast.makeText(getContext(), "Invite(s) replaced", Toast.LENGTH_SHORT).show();
    }

    /**
     * Converts a UserList (which may contain raw map objects from Firestore) into concrete Users.
     * @param list raw invited/cancelled list.
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

    /** Removes a user with a matching id from the provided list, if present.
     * @param list list to mutate.
     * @param id user id to remove.
     */
    private void removeById(UserList list, String id) {
        if (list == null || list.getUsers() == null) {
            return;
        }
        List<User> users = list.getUsers();
        for (int i = users.size() - 1; i >= 0; i--) {
            User user = users.get(i);
            if (user != null && Objects.equals(user.getId(), id)) {
                users.remove(i);
            }
        }
    }

    /** Adds a user if not already in the provided list (by id).
     * @param list list to mutate.
     * @param user user to add if missing.
     */
    private void addIfMissing(UserList list, User user) {
        if (list == null || user == null) {
            return;
        }
        for (User existing : list.getUsers()) {
            if (existing != null && Objects.equals(existing.getId(), user.getId())) {
                return;
            }
        }
        list.add(user);
    }
}
