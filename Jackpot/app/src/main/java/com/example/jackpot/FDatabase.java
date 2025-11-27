package com.example.jackpot;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: FDatabase.java
 *
 * Purpose/Role:
 *   Lightweight data-access facade over Firebase Firestore for app domain objects.
 *   Centralizes common queries and hides Firestore boilerplate.
 *
 * Design Pattern:
 *   - Singleton: a single shared instance via getInstance().
 */

/**
 * Singleton facade for Firestore access used by the application.
 * Provides typed callbacks and convenience methods for common queries and updates.
 */
public class FDatabase {
    private static FDatabase instance = null;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FDatabase() {}

    /**
     * Get the singleton instance of the database.
     * If the instance doesn't exist, create it.
     * @return The singleton instance of the database.
     */
    public static FDatabase getInstance() {
        if (instance == null) {
            instance = new FDatabase();
        }
        return instance;
    }

    /**
     * Get the Firebase Firestore instance used by the database.
     * @return The Firebase Firestore instance.
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    // Generic callback interface
    public interface DataCallback<T> {
        void onSuccess(ArrayList<T> data);
        void onFailure(Exception e);
    }

    /**
     * Callback interface for successful and failed operations.
     */
    public interface StatusCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Single event callback interface

    /**
     * Callback interface for events.
     */
    public interface EventCallback {
        void onSuccess(Event event);
        void onFailure(String error);
    }

    /**
     * Get a single event by its ID from Firestore
     * Function creation assisted using Gemini
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
     * Function creation assisted using Gemini
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
     * Function creation assisted using Gemini
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
     * Function creation assisted using Gemini
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

    /**
     * Updates an event in Firestore
     * @param event The event to update
     */
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

    /**
     * Queries events based on a field and value
     * @param field parameter to compare
     * @param value value to check in parameter
     * @param callback Callback to handle success or failure
     */
    public void queryEvents(String field, Object value, DataCallback<Event> callback) {
        queryCollection("events", field, value, Event.class, callback);
    }

    /**
     * Queries events where a field (array) contains a specific value.
     * @param field parameter to compare (must be an array)
     * @param value value to check for in the array
     * @param callback Callback to handle success or failure
     */
    public void queryEventsWithArrayContains(String field, Object value, DataCallback<Event> callback) {
        queryCollectionWithArrayContains("events", field, value, Event.class, callback);
    }

    /**
     * Gets all events from the "events" collection
     * @param callback Callback to handle success or failure
     */
    public void getAllEvents(DataCallback<Event> callback) {
        getAllFromCollection("events", Event.class, callback);
    }

    // Convenience methods for Users

    /**
     * Queries users based on a field and value
     *
     * @param uid The ID of the user to query
     * @param callback Callback to handle success or failure
     */
    public void getUserById(String uid, DataCallback<User> callback) {
        queryCollection("users", "id", uid, User.class, callback);
    }


    /**
     * Deletes an event from Firestore
     * @param eventId The ID of the event to delete
     * @param callback Callback to handle success or failure
     */
    public void deleteEvent(String eventId, StatusCallback callback) {
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }


    /**
     * Queries events created by a specific organizer.
     *
     * @param creatorId The ID of the organizer who created the events
     * @param callback Callback to handle the results
     */
    public void queryEventsByCreator(String creatorId, DataCallback<Event> callback) {
        queryCollection("events", "organizerId", creatorId, Event.class, callback);
    }
}
