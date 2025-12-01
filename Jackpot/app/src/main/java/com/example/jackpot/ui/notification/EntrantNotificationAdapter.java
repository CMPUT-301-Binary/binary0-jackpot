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
import com.google.firebase.Timestamp;

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
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_event_notification, parent, false);

            holder = new ViewHolder();
            holder.dateText = convertView.findViewById(R.id.dateText);
            holder.messageText = convertView.findViewById(R.id.notif_message);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notif = notifications.get(position);

        // ---------- DATE ----------
        Timestamp ts = notif.getSentAt();
        if (ts != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                            .withZone(ZoneId.systemDefault());
            holder.dateText.setText(formatter.format(ts.toDate().toInstant()));
        } else {
            holder.dateText.setText("");
        }

        // ---------- MESSAGE ----------
        holder.messageText.setText(
                notif.getPayload() != null ? notif.getPayload() : "Notification"
        );

        return convertView;
    }



    /**
     * Marks a notification as read and removes it from the display.
     * The notification remains in the database but is marked as read.
     * @param position The position of the notification to mark as read
     */
    public void markNotificationAsRead(int position) {
        if (position < 0 || position >= notifications.size()) return;

        Notification notif = notifications.get(position);
        String id = notif.getNotificationID();

        notifications.remove(position);
        notifyDataSetChanged();

        if (id != null && !id.isEmpty()) {
            fDatabase.getDb().collection("notifications")
                    .document(id)
                    .update("viewedByEntrant", true)
                    .addOnSuccessListener(a -> Log.d(TAG, "Marked read"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed mark read", e));
        }
    }

    /**
     * ViewHolder pattern to improve ListView performance by caching view references.
     */
    private static class ViewHolder {
        TextView dateText;
        TextView messageText;
    }
}
