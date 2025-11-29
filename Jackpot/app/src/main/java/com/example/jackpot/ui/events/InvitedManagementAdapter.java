package com.example.jackpot.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.R;
import com.example.jackpot.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Adapter used by organizers to review invited entrants and select which invitations to cancel.
 */
public class InvitedManagementAdapter extends RecyclerView.Adapter<InvitedManagementAdapter.InvitedViewHolder> {

    private final ArrayList<User> invitedUsers;
    private final ArrayList<User> selectedUsers = new ArrayList<>();

    public InvitedManagementAdapter(ArrayList<User> invitedUsers) {
        this.invitedUsers = invitedUsers != null ? invitedUsers : new ArrayList<>();
    }

    @NonNull
    @Override
    public InvitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invited_user, parent, false);
        return new InvitedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitedViewHolder holder, int position) {
        User user = invitedUsers.get(position);
        holder.nameView.setText(user.getName() != null ? user.getName() : "Unnamed entrant");
        holder.emailView.setText(user.getEmail() != null ? user.getEmail() : "No email");

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(isSelected(user));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!isSelected(user)) {
                    selectedUsers.add(user);
                }
            } else {
                removeSelectedById(user.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return invitedUsers.size();
    }

    public ArrayList<User> getSelectedUsers() {
        return new ArrayList<>(selectedUsers);
    }

    public void toggleSelectAll() {
        if (selectedUsers.size() == invitedUsers.size()) {
            selectedUsers.clear();
        } else {
            selectedUsers.clear();
            selectedUsers.addAll(invitedUsers);
        }
        notifyDataSetChanged();
    }

    public void removeUsers(List<User> usersToRemove) {
        if (usersToRemove == null || usersToRemove.isEmpty()) {
            return;
        }
        Iterator<User> iterator = invitedUsers.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (containsUser(usersToRemove, user)) {
                iterator.remove();
            }
        }
        for (User user : usersToRemove) {
            removeSelectedById(user.getId());
        }
        notifyDataSetChanged();
    }

    public void addUsers(List<User> newUsers) {
        if (newUsers == null || newUsers.isEmpty()) {
            return;
        }
        for (User user : newUsers) {
            if (!containsUser(invitedUsers, user)) {
                invitedUsers.add(user);
            }
        }
        notifyDataSetChanged();
    }

    private boolean containsUser(List<User> list, User target) {
        for (User user : list) {
            if (Objects.equals(user.getId(), target.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSelected(User user) {
        return containsUser(selectedUsers, user);
    }

    private void removeSelectedById(String id) {
        Iterator<User> iterator = selectedUsers.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (Objects.equals(user.getId(), id)) {
                iterator.remove();
            }
        }
    }

    static class InvitedViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView emailView;
        CheckBox checkBox;

        InvitedViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.invited_name);
            emailView = itemView.findViewById(R.id.invited_email);
            checkBox = itemView.findViewById(R.id.checkbox_select);
        }
    }
}
