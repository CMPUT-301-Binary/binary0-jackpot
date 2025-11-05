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

    /**
     * retrieves events from the database based on the given parameters
     * @param param parameter to compare
     * @param value value to check in parameter
     * @return ArrayList of Event objects
     */
    public ArrayList<Event> queryEvents(String param, String value) {
        ArrayList<Event> results = new ArrayList<>();
        Query query = db.collection("events").orderBy(param).whereEqualTo(value);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Event event = dataSnapshot.getValue(Event.class);
                        results.add(event);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
                // handle error
                // int errorCode = error.getCode();
                // act accordingly
            }
        });
        return results;
    }
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> results = new ArrayList<>();
        db.collection("events").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot dataDocumentSnapshots) {
                        if (!dataDocumentSnapshots.isEmpty()) {
                            // Handle no data
                            Log.d("FDatabase", "No data found");
                        } else {
                            for (DataSnapshot dataSnapshot : dataDocumentSnapshots.getChildren()) {
                                Event event = dataSnapshot.getValue(Event.class);
                                results.add(event);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        e.printStackTrace();
                    }
                });
        return results;
    }

}
