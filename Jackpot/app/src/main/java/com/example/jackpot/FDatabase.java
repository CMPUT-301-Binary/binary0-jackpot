package com.example.jackpot;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

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

    // Generic callback interface
    public interface DataCallback<T> {
        void onSuccess(ArrayList<T> data);
        void onFailure(Exception e);
    }

    // Single event callback interface
    public interface EventCallback {
        void onSuccess(Event event);
        void onFailure(String error);
    }

    /**
     * Get a single event by its ID from Firestore
     * @param eventId The ID of the event to retrieve
     * @param callback Callback to handle success or failure
     */
    public void getEventById(String eventId, EventCallback callback) {
        if (eventId == null || eventId.isEmpty()) {
            callback.onFailure("Event ID is null or empty");
            return;
        }

        Log.d("FDatabase", "Fetching event with ID: " + eventId);

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Event event = documentSnapshot.toObject(Event.class);
                            if (event != null) {
                                // Make sure the eventId is set
                                if (event.getEventId() == null || event.getEventId().isEmpty()) {
                                    event.setEventId(documentSnapshot.getId());
                                }
                                Log.d("FDatabase", "Event found: " + event.getName());
                                callback.onSuccess(event);
                            } else {
                                Log.e("FDatabase", "Event object is null after conversion");
                                callback.onFailure("Failed to parse event data");
                            }
                        } catch (Exception e) {
                            Log.e("FDatabase", "Error parsing event: " + e.getMessage());
                            callback.onFailure("Error parsing event: " + e.getMessage());
                        }
                    } else {
                        Log.e("FDatabase", "Event document does not exist for ID: " + eventId);
                        callback.onFailure("Event not found in database");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FDatabase", "Error fetching event: " + e.getMessage());
                    callback.onFailure("Database error: " + e.getMessage());
                });
    }

    /**
     * Queries documents from any collection based on field and value
     * @param collectionName name of the collection to query
     * @param field parameter to compare
     * @param value value to check in parameter
     * @param classType the class type to convert documents to
     * @param callback callback to handle results
     */
    public <T> void queryCollection(String collectionName, String field, Object value,
                                    Class<T> classType, DataCallback<T> callback) {
        db.collection(collectionName).whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<T> results = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            T item = documentSnapshot.toObject(classType);
                            if (item != null) {
                                results.add(item);
                            }
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(e);
                });
    }

    /**
     * Queries documents from any collection where a field (array) contains a specific value.
     * @param collectionName name of the collection to query
     * @param field parameter to compare (must be an array)
     * @param value value to check for in the array
     * @param classType the class type to convert documents to
     * @param callback callback to handle results
     */
    public <T> void queryCollectionWithArrayContains(String collectionName, String field, Object value,
                                                     Class<T> classType, DataCallback<T> callback) {
        db.collection(collectionName).whereArrayContains(field, value)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<T> results = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            T item = documentSnapshot.toObject(classType);
                            if (item != null) {
                                results.add(item);
                            }
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(e);
                });
    }

    /**
     * Gets all documents from any collection
     * @param collectionName name of the collection
     * @param classType the class type to convert documents to
     * @param callback callback to handle results
     */
    public <T> void getAllFromCollection(String collectionName, Class<T> classType,
                                         DataCallback<T> callback) {
        db.collection(collectionName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<T> results = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FDatabase", "No data found in " + collectionName);
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            T item = documentSnapshot.toObject(classType);
                            if (item != null) {
                                results.add(item);
                            }
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onFailure(e);
                });
    }

    public void updateEvent(Event event) {
        if (event == null || event.getEventId() == null) {
            Log.e("FDatabase", "Event or event ID is null, cannot update.");
            return;
        }
        db.collection("events").document(event.getEventId()).set(event)
                .addOnSuccessListener(aVoid -> Log.d("FDatabase", "Event updated successfully"))
                .addOnFailureListener(e -> Log.e("FDatabase", "Error updating event", e));
    }

    // Convenience methods for Events (backward compatibility)
    public void queryEvents(String field, Object value, DataCallback<Event> callback) {
        queryCollection("events", field, value, Event.class, callback);
    }

    public void queryEventsWithArrayContains(String field, Object value, DataCallback<Event> callback) {
        queryCollectionWithArrayContains("events", field, value, Event.class, callback);
    }

    public void getAllEvents(DataCallback<Event> callback) {
        getAllFromCollection("events", Event.class, callback);
    }

    // Convenience methods for Users
    public void getUserById(String uid, DataCallback<User> callback) {
        queryCollection("users", "id", uid, User.class, callback);
    }
}