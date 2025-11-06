package com.example.jackpot.ui.profile;

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
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private final List<User> userList;
    private final List<User> selectedUsers = new ArrayList<>();
    private boolean allSelected = false;

    public ProfileAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.role.setText(user.getRole().toString().toLowerCase());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedUsers.contains(user));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUsers.contains(user))
                    selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<User> getSelectedUsers() {
        return new ArrayList<>(selectedUsers);
    }

    public void toggleSelectAll() {
        allSelected = !allSelected;
        selectedUsers.clear();
        if (allSelected) {
            selectedUsers.addAll(userList);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            role = itemView.findViewById(R.id.profile_role);
            checkBox = itemView.findViewById(R.id.checkbox_select);
        }
    }
}

