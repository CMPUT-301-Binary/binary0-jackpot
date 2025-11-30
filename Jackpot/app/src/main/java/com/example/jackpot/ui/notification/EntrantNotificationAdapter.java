package com.example.jackpot.ui.notification;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.jackpot.Event;
import com.example.jackpot.FDatabase;
import com.example.jackpot.Notification;
import com.example.jackpot.R;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


/**
 * Created with the assistance of ClaudeAI Sonnet 4.5
 */
public class EntrantNotificationAdapter extends ArrayAdapter<Notification> {
    private static final String TAG = "EntrantNotifAdapter";
    private Context context;
    private ArrayList<Notification> notifications;
    private FDatabase fDatabase;

    /**
     * Constructor for the adapter.
     * @param context The context
     * @param notifications List of notifications to display
     */
    public EntrantNotificationAdapter(@NonNull Context context, ArrayList<Notification> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifications = notifications;
        this.fDatabase = FDatabase.getInstance();

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notification_event_item, parent, false);
            holder = new ViewHolder();
            holder.eventText = convertView.findViewById(R.id.event_text);
            holder.statusText = convertView.findViewById(R.id.statusText);
            holder.categoryText = convertView.findViewById(R.id.categoryText);
            holder.eventPrice = convertView.findViewById(R.id.eventPrice);
            holder.eventPic = convertView.findViewById(R.id.event_pic);
            holder.buttonContainer = convertView.findViewById(R.id.button_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notification = notifications.get(position);

        if (notification != null) {
            // Set notification type as category
            holder.categoryText.setText(notification.getNotifType() != null ? notification.getNotifType() : "Notification");

            // Set notification payload/message as status
            holder.statusText.setText(notification.getPayload() != null ? notification.getPayload() : "");

            // Format and show the sent time
            if (notification.getSentAt() != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                        .withZone(ZoneId.systemDefault());
                String formattedDate = formatter.format(notification.getSentAt());
                holder.eventPrice.setText(formattedDate);
            } else {
                holder.eventPrice.setText("");
            }

            // Hide button container for notifications
            if (holder.buttonContainer != null) {
                holder.buttonContainer.setVisibility(View.GONE);
            }

            // Fetch and display event details if eventID exists
            String eventId = notification.getEventID();
            if (eventId != null && !eventId.isEmpty()) {
                loadEventDetails(holder, eventId);
            } else {
                // No event associated, show generic notification info
                holder.eventText.setText("General Notification");
                holder.eventPic.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        return convertView;
    }

    /**
     * Loads event details from Firebase and populates the view.
     * @param holder The ViewHolder containing the views to populate
     * @param eventId The ID of the event to fetch
     */
    private void loadEventDetails(ViewHolder holder, String eventId) {
        fDatabase.getEventById(eventId, new FDatabase.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (event != null) {
                    // Set event name
                    holder.eventText.setText(event.getName() != null ? event.getName() : "Event");
                    // Set event category
                    holder.categoryText.setText(event.getCategory() != null ? event.getCategory() : "Category");


                    // Load event image using Glide
                    if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
                        Glide.with(context)
                                .load(event.getPosterUri())
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(holder.eventPic);
                    } else {
                        holder.eventPic.setImageResource(R.drawable.ic_launcher_background);
                    }
                } else {
                    Log.w(TAG, "Event is null for eventId: " + eventId);
                    holder.eventText.setText("Event Details Unavailable");
                    holder.eventPic.setImageResource(R.drawable.ic_launcher_background);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load event: " + error);
                // Show placeholder if event can't be loaded
                holder.eventText.setText("Event Details Unavailable");
                holder.eventPic.setImageResource(R.drawable.ic_launcher_background);
            }
        });
    }

    /**
     * Marks a notification as read and removes it from the display.
     * The notification remains in the database but is marked as read.
     * @param position The position of the notification to mark as read
     */
    public void markNotificationAsRead(int position) {
        if (position >= 0 && position < notifications.size()) {
            Notification notification = notifications.get(position);
            String notificationId = notification.getNotificationID();

            // Remove from local list immediately for responsive UI
            notifications.remove(position);
            notifyDataSetChanged();

            // Mark as read in Firebase (keeps the notification in database)
            if (notificationId != null && !notificationId.isEmpty()) {
                fDatabase.getDb().collection("notifications")
                        .document(notificationId)
                        .update("viewedByEntrant", true)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as read", e));
            }
        }
    }

    /**
     * ViewHolder pattern to improve ListView performance by caching view references.
     */
    private static class ViewHolder {
        TextView eventText;
        TextView statusText;
        TextView categoryText;
        TextView eventPrice;
        ImageView eventPic;
        View buttonContainer;
    }
}
