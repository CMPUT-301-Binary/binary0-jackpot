package com.example.jackpot.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.Notification;
import com.example.jackpot.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventNotificationFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";

    private String eventId, eventName;

    private RecyclerView recyclerView;
    private OrganizerEventNotificationAdapter adapter;
    private FirebaseFirestore db;

    public static OrganizerEventNotificationFragment newInstance(String eventId, String eventName) {
        OrganizerEventNotificationFragment f = new OrganizerEventNotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_event_notification, container, false);
        Button backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        db = FirebaseFirestore.getInstance();

        recyclerView = root.findViewById(R.id.event_notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new OrganizerEventNotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }

        loadEventNotifications();
        return root;
    }

    private void loadEventNotifications() {
        if (eventId == null || eventId.isEmpty()) {
            return;
        }

        db.collection("notifications")
                .whereEqualTo("eventID", eventId)
                .get()
                .addOnSuccessListener(query -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Notification n = doc.toObject(Notification.class);
                        list.add(n);
                    }
                    adapter.updateData(list);
                })
                .addOnFailureListener(e -> {
                    // you can log / toast if you want
                });
    }
}
