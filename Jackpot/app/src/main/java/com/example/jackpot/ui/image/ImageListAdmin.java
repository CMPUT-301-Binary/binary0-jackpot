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
     * Loads the images,a nd sets up the list.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI, or null.
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

    /**
     * Loads the images from the database.
     */
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
                            doc.getReference().delete();
                            Log.d("Firestore", "Deleted Firestore document: " + doc.getId());
                        }
                        allImages.remove(image);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Failed to delete Firestore record", e));

            // Delete from Storage
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
            // If the deleted image was a user profile, reset to default
            firestore.collection("users")
                    .whereEqualTo("profileImageUrl", imageUrl)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        for (DocumentSnapshot userDoc : userSnapshot) {
                            userDoc.getReference().update("profileImageUrl", "default");
                            Log.d("Firestore", "Reset user profile image to default for: " + userDoc.getId());
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("Firestore", "Failed to reset profile image", e));

        }

        Toast.makeText(requireContext(), "Selected images deleted", Toast.LENGTH_SHORT).show();
    }
}
