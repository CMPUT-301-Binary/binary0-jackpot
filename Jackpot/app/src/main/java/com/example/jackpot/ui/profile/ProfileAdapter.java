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

/**
 * Adapter for displaying user profiles, and profile information.
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private final List<User> userList;
    private final List<User> selectedUsers = new ArrayList<>();
    private boolean allSelected = false;

    /**
     * Constructor for ProfileAdapter.
     * @param userList List of users to display.
     */
    public ProfileAdapter(List<User> userList) {
        this.userList = userList;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * Inflates the view.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Sets the data for the ViewHolder.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     *
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The number of items in the dataset
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Returns the list of selected users.
     *
     * @return The list of selected users
     */
    public List<User> getSelectedUsers() {
        return new ArrayList<>(selectedUsers);
    }

    /**
     * Clears the list of selected users.
     */
    public void toggleSelectAll() {
        allSelected = !allSelected;
        selectedUsers.clear();
        if (allSelected) {
            selectedUsers.addAll(userList);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for displaying user profiles.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role;
        CheckBox checkBox;

        /**
         * Constructor for ViewHolder.
         * Initializes views.
         * @param itemView The view representing the item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            role = itemView.findViewById(R.id.profile_role);
            checkBox = itemView.findViewById(R.id.checkbox_select);
        }
    }
}

