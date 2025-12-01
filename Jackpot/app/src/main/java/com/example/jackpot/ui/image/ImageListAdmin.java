package com.example.jackpot.ui.image;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of images the admin is able to see, and delete if the admin wants.
 */
public class ImageListAdmin extends Fragment {

    private RecyclerView recyclerView;
    private ImageListAdapter adapter;
    private Button buttonSelectAll, buttonDelete;
    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private final List<Image> allImages = new ArrayList<>();

    /**
     * Inflate the admin image list, load images, and wire select/delete actions.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_image_list_admin, container, false);

        recyclerView = root.findViewById(R.id.image_recycler_view);
        buttonSelectAll = root.findViewById(R.id.button_select_all_image);
        buttonDelete = root.findViewById(R.id.button_delete_image);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ImageListAdapter(allImages);
        recyclerView.setAdapter(adapter);

        loadImages();

        buttonSelectAll.setOnClickListener(v -> adapter.toggleSelectAll());
        buttonDelete.setOnClickListener(v -> deleteSelectedImages());

        return root;
    }

    /** Loads non-QR images (and profile images) from Firestore into the adapter. */
    private void loadImages() {
        allImages.clear();  // clear old data
        db.collection("images")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Image image = doc.toObject(Image.class);

                        // skip QR code images
                        if (Image.TYPE_QR_CODE.equals(image.getImageType())) {
                            continue;
                        }

                        allImages.add(image);
                    }
                    db.collection("users")
                            .get()
                            .addOnSuccessListener(userSnapshot -> {
                                for (QueryDocumentSnapshot userDoc : userSnapshot) {
                                    String profileUrl = userDoc.getString("profileImageUrl");
                                    if (profileUrl != null && !profileUrl.isEmpty() && !profileUrl.equals("default")) {
                                        Image profileImage = new Image();
                                        profileImage.setImageUrl(profileUrl);
                                        profileImage.setUploadedBy(userDoc.getId());
                                        profileImage.setImageID("profile_" + userDoc.getId());
                                        allImages.add(profileImage);
                                    }
                                }
                                adapter.notifyDataSetChanged();  // Update UI after ALL DONE
                            });
                });
    }

    /**
     * Deletes the selected images.
     */
    private void deleteSelectedImages() {
        List<Image> selected = adapter.getSelectedImages();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "No images selected", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://jackpot-d3153.appspot.com");

        for (Image image : selected) {
            String imageId = image.getImageID();
            String imageUrl = image.getImageUrl();

            // Delete Firestore document by field match
            firestore.collection("images")
                    .whereEqualTo("imageID", imageId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot doc : querySnapshot) {
                            // DELETE THE IMAGE DOCUMENT
                            doc.getReference().delete();
                            Log.d("Firestore", "Deleted Firestore document: " + doc.getId());

                            // UPDATE EVENT TO REMOVE POSTER
                            String eventId = doc.getString("eventId");   // Now doc exists
                            if (eventId != null) {
                                firestore.collection("events")
                                        .document(eventId)
                                        .update("posterUri", "default")
                                        .addOnSuccessListener(v ->
                                                Log.d("EventUpdate", "Cleared poster for event " + eventId));
                            }
                        }
                        allImages.remove(image);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Failed to delete Firestore record", e));

            // Delete from Firebase Storage
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                    imageRef.delete()
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Storage", "Deleted from storage: " + imageUrl))
                            .addOnFailureListener(e ->
                                    Log.e("Storage", "Failed to delete from storage: " + e.getMessage()));
                } catch (Exception e) {
                    Log.e("Storage", "Invalid Storage URL: " + imageUrl, e);
                }
            }

            // Reset USER profile images if matched
            firestore.collection("users")
                    .whereEqualTo("profileImageUrl", imageUrl)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        for (DocumentSnapshot userDoc : userSnapshot) {
                            userDoc.getReference().update("profileImageUrl", "default");
                            Log.d("Firestore", "Reset profile image for: " + userDoc.getId());
                        }
                    });

            // Clear POSTER and QR code fields in EVENTS
            String eventId = image.getEventId();
            if (eventId != null && !eventId.isEmpty()) {

                // If this is a POSTER
                if (Image.TYPE_POSTER.equals(image.getImageType())) {
                    firestore.collection("events")
                            .document(eventId)
                            .update("posterUri", "default")
                            .addOnSuccessListener(a ->
                                    Log.d("EventUpdate", "Cleared poster for event " + eventId));
                }

            }
            Toast.makeText(requireContext(), "Selected images deleted", Toast.LENGTH_SHORT).show();
        }
    }
}
