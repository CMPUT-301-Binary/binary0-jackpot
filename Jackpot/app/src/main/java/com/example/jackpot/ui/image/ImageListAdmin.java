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
import com.example.jackpot.Image;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageListAdmin extends Fragment {

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private Button buttonSelectAll, buttonDelete;
    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private final List<Image> allImages = new ArrayList<>();

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
        adapter = new ImageAdapter(allImages);
        recyclerView.setAdapter(adapter);

        loadImages();

        buttonSelectAll.setOnClickListener(v -> adapter.toggleSelectAll());
        buttonDelete.setOnClickListener(v -> deleteSelectedImages());

        return root;
    }

    private void loadImages() {
        db.collection("images")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allImages.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Image image = doc.toObject(Image.class);
                        allImages.add(image);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Failed to load images: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

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

            // Step 1: Delete Firestore document by field match
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

            // Step 2: Delete from Storage
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
        }

        Toast.makeText(requireContext(), "Selected images deleted", Toast.LENGTH_SHORT).show();
    }
}
