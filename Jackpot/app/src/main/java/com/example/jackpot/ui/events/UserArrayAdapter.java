package com.example.jackpot.ui.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.R;
import com.example.jackpot.User;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for displaying a list of users in a RecyclerView.
 * Used in fragments like WaitingListFragment and CancelListFragment to show entrants.
 * Class functionality partially assisted by Gemini
 */
public class UserArrayAdapter extends RecyclerView.Adapter<UserArrayAdapter.UserViewHolder> {

    private final ArrayList<User> userList;
    private final Context context;

    /**
     * Constructs a UserArrayAdapter.
     * @param context The context from the calling fragment or activity.
     * @param userList The list of users to display.
     */
    public UserArrayAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    /**
     * Called when RecyclerView needs a new {@link UserViewHolder} of the given type to represent
     * an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A new UserViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.waiting_list_item, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The UserViewHolder which should be updated to represent the contents of the
     *               item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        if (user != null) {
            holder.attendeeName.setText(user.getName());
            holder.notifyButton.setOnClickListener(v -> {
                // TODO: Implement actual notification logic for a single user
                Toast.makeText(context, "Notifying " + user.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    /**
     * ViewHolder class for each user item in the RecyclerView.
     * Holds references to the UI components for a single item.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView attendeeName;
        Button notifyButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            attendeeName = itemView.findViewById(R.id.attendee_name);
            notifyButton = itemView.findViewById(R.id.notify_button);
        }
    }
}
