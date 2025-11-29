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
 * Adapter for displaying notification details in a RecyclerView.
 */
public class AdminNotificationDetailAdapter extends RecyclerView.Adapter<AdminNotificationDetailAdapter.NotificationViewHolder> {

    private List<AdminDetailedNotificationFragment.NotificationDetail> notificationList;

    /**
     * Constructor for the adapter.
     * @param notificationList List of notifications to display
     */
    public AdminNotificationDetailAdapter(List<AdminDetailedNotificationFragment.NotificationDetail> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AdminDetailedNotificationFragment.NotificationDetail notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * Updates the adapter's data and refreshes the RecyclerView.
     * @param newList New list of notifications
     */
    public void updateData(List<AdminDetailedNotificationFragment.NotificationDetail> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for notification items.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView notificationText;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationText = itemView.findViewById(R.id.notification_text);
        }

        /**
         * Binds notification data to the view.
         * @param notification The notification to display
         */
        public void bind(AdminDetailedNotificationFragment.NotificationDetail notification) {
            // Build a formatted notification message
            StringBuilder displayText = new StringBuilder();

            // Add event name
            if (notification.getEventName() != null) {
                displayText.append("Event: ").append(notification.getEventName()).append("\n");
            }

            // Add payload/message
            if (notification.getPayload() != null) {
                displayText.append("Message: ").append(notification.getPayload());
            }

            notificationText.setText(displayText.toString());
        }
    }
}