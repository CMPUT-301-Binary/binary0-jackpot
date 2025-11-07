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
import com.example.jackpot.Image;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final List<Image> imageList;
    private final List<Image> selectedImages = new ArrayList<>();
    private boolean allSelected = false;

    public ImageAdapter(List<Image> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Image image = imageList.get(position);
        String imageUrl = image.getImageUrl();

        holder.email.setText("Uploaded by: " + image.getUploadedBy());

        if (imageUrl != null && imageUrl.startsWith("gs://")) {
            try {
                StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("ImageAdapter", "Resolved URL: " + uri.toString());
                    Glide.with(holder.itemView.getContext())
                            .load(uri.toString())
                            .placeholder(R.drawable._ukj7h)
                            .error(R.drawable._ukj7h)
                            .into(holder.image);
                }).addOnFailureListener(e -> {
                    Log.e("ImageAdapter", "Failed to resolve gs:// link", e);
                    holder.image.setImageResource(R.drawable._ukj7h);
                });
            } catch (Exception e) {
                Log.e("ImageAdapter", "Invalid Firebase Storage reference", e);
                holder.image.setImageResource(R.drawable._ukj7h);
            }
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable._ukj7h)
                    .error(R.drawable._ukj7h)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable._ukj7h);
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

