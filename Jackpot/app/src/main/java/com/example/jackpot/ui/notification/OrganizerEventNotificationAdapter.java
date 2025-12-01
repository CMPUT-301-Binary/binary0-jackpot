package com.example.jackpot.ui.notification;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.Notification;
import com.example.jackpot.R;
import com.google.firebase.Timestamp;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapter for displaying event-specific notifications to organizers.
 * Shows notification date and message in a RecyclerView.
 */
public class OrganizerEventNotificationAdapter
        extends RecyclerView.Adapter<OrganizerEventNotificationAdapter.ViewHolder> {

    private List<Notification> notifList;

    public OrganizerEventNotificationAdapter(List<Notification> notifList) {
        this.notifList = notifList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifList.get(position);

        // Format date if available
        Timestamp ts = notif.getSentAt();
        if (ts != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());
            holder.dateText.setText(formatter.format(ts.toDate().toInstant()));
        } else {
            holder.dateText.setText("");
        }

        // Set notification message
        holder.messageText.setText(
                notif.getPayload() != null ? notif.getPayload() : "Notification"
        );
    }

    @Override
    public int getItemCount() {
        return notifList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView messageText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Match the IDs from item_event_notification.xml
            dateText = itemView.findViewById(R.id.dateText);
            messageText = itemView.findViewById(R.id.notif_message);
        }
    }

    /**
     * Updates the adapter with new notification data.
     * @param list New list of notifications to display
     */
    public void updateData(List<Notification> list) {
        notifList = list;
        notifyDataSetChanged();
    }
}