package com.example.jackpot;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FDatabase {
    private static FDatabase instance = null;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FDatabase() {}
    public static FDatabase getInstance() {
        if (instance == null) {
            instance = new FDatabase();
        }
        return instance;
    }
    public FirebaseFirestore getDb() {
        return db;
    }


    public interface EventCallback {
        void onSuccess(ArrayList<Event> events);
        void onFailure(Exception e);
    }

    /**
     * retrieves events from the database based on the given parameters
     * @param field parameter to compare
     * @param value value to check in parameter
     * @return ArrayList of Event objects
     */

    // Modified queryEvents with callback
    public void queryEvents(String field, Object value, EventCallback callback) {
        db.collection("events").whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Event> results = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Event event = documentSnapshot.toObject(Event.class);
                            results.add(event);
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(e);
                });
    }

    // Fixed getAllEvents with callback
    public void getAllEvents(EventCallback callback) {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Event> results = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FDatabase", "No data found");
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Event event = documentSnapshot.toObject(Event.class);
                            results.add(event);
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(e);
                });
    }
}
