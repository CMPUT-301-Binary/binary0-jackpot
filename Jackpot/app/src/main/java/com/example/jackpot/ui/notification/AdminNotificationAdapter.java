package com.example.jackpot.ui.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.R;

import java.util.List;

/**
 * Adapter for displaying organizer information in a RecyclerView.
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.AdminNotificationViewHolder> {

    private List<NotificationFragment.OrganizerInfo> organizerList;
    private OnOrganizerClickListener clickListener;

    /**
     * Interface for handling organizer item clicks.
     */
    public interface OnOrganizerClickListener {
        void onOrganizerClick(NotificationFragment.OrganizerInfo organizer);
    }

    /**
     * Constructor for the adapter.
     * @param organizerList List of organizers to display
     */
    public AdminNotificationAdapter(List<NotificationFragment.OrganizerInfo> organizerList) {
        this.organizerList = organizerList;
    }

    /**
     * Sets the click listener for organizer items.
     * @param listener The click listener
     */
    public void setOnOrganizerClickListener(OnOrganizerClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public AdminNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer_names, parent, false);
        return new AdminNotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminNotificationViewHolder holder, int position) {
        NotificationFragment.OrganizerInfo organizer = organizerList.get(position);
        holder.bind(organizer, clickListener);
    }

    @Override
    public int getItemCount() {
        return organizerList.size();
    }

    /**
     * Updates the adapter's data and refreshes the RecyclerView.
     * @param newList New list of organizers
     */
    public void updateData(List<NotificationFragment.OrganizerInfo> newList) {
        this.organizerList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for organizer items.
     */
    static class AdminNotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView nameLabel;
        private TextView emailText;

        public AdminNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameLabel = itemView.findViewById(R.id.text_organizer_name_label);
            emailText = itemView.findViewById(R.id.text_organizer_email);
        }

        /**
         * Binds organizer data to the views and sets up click listener.
         * @param organizer The organizer information to display
         * @param clickListener The click listener for the item
         */
        public void bind(NotificationFragment.OrganizerInfo organizer, OnOrganizerClickListener clickListener) {
            nameLabel.setText(organizer.getName());
            emailText.setText(organizer.getEmail());

            // Set click listener on the entire item view
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onOrganizerClick(organizer);
                }
            });
        }
    }
}