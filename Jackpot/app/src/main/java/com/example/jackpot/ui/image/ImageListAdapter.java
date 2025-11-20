package com.example.jackpot.ui.image;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jackpot.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    private final List<Image> imageList;
    private final List<Image> selectedImages = new ArrayList<>();
    private boolean allSelected = false;

    public ImageListAdapter(List<Image> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Image image = imageList.get(position);
        String imageUrl = image.getImageUrl();
        String uploaderId = image.getUploadedBy();

        // Get email from Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uploaderId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("email");
                        if (email != null) {
                            holder.email.setText("Uploaded by: " + email);
                        }
                    }
                });

        if (imageUrl != null && imageUrl.startsWith("gs://")) {
            try {
                StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("ImageAdapter", "Resolved URL: " + uri.toString());
                    Glide.with(holder.itemView.getContext())
                            .load(uri.toString())
                            .placeholder(R.drawable.avatar_1)
                            .error(R.drawable.avatar_1)
                            .into(holder.image);
                }).addOnFailureListener(e -> {
                    Log.e("ImageAdapter", "Failed to resolve gs:// link", e);
                    holder.image.setImageResource(R.drawable.avatar_1);
                });
            } catch (Exception e) {
                Log.e("ImageAdapter", "Invalid Firebase Storage reference", e);
                holder.image.setImageResource(R.drawable.avatar_1);
            }
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.avatar_1)
                    .error(R.drawable.avatar_1)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.avatar_1);
        }

        // Checkbox handling
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedImages.contains(image));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedImages.contains(image))
                    selectedImages.add(image);
            } else {
                selectedImages.remove(image);
            }
        });
    }


    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public List<Image> getSelectedImages() {
        return new ArrayList<>(selectedImages);
    }

    public void toggleSelectAll() {
        allSelected = !allSelected;
        selectedImages.clear();
        if (allSelected) selectedImages.addAll(imageList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView email;
        ImageView image;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.uploaded_image);
            email = itemView.findViewById(R.id.profile_email);
            checkBox = itemView.findViewById(R.id.checkbox_select_image);
        }
    }
}

