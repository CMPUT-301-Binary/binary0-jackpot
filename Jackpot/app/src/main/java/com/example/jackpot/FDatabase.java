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
        queryCollection("users", "userId", uid, User.class, callback);
    }
}
