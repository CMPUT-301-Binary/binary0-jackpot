package com.example.jackpot.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.jackpot.FDatabase;

import com.example.jackpot.R;
import com.example.jackpot.activities.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * ProfileFragment, which actually displays the profile information, and has functionalities.
 * These functionalities include delete account, logout button, image picker, and showing the profile details
 */
public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private EditText nameField, emailField, phoneField, bioField;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Button deleteAccountButton;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Called to have the fragment instantiate its user interface view.
     * Authenticates the firebase instance and loads the info to the view
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate once
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI references
        profileImage = root.findViewById(R.id.profile_image);
        nameField = root.findViewById(R.id.profile_name);
        emailField = root.findViewById(R.id.profile_email);
        phoneField = root.findViewById(R.id.profile_phone);
        bioField = root.findViewById(R.id.profile_bio);
        logoutButton = root.findViewById(R.id.logout_button);
        deleteAccountButton = root.findViewById(R.id.delete_account_button);
        Button saveProfileButton = root.findViewById(R.id.save_profile_button);
        saveProfileButton.setOnClickListener(v -> saveUserProfile());


        // Load user info
        loadUserProfile();

        // Image picker setup
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        profileImage.setImageURI(selectedImageUri);
                    }
                });

        // Open gallery on image click
        profileImage.setOnClickListener(v -> openGallery());

        // Logout and Delete account
        logoutButton.setOnClickListener(v -> logout());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

        return root;
    }

    // 🔹 Fetch data from Firestore

    /**
     * Load the user profile information from Firestore.
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        nameField.setText(snapshot.getString("name"));
                        emailField.setText(snapshot.getString("email"));
                        phoneField.setText(snapshot.getString("phone"));
                        bioField.setText(snapshot.getString("notificationPreferences")); // reuse as bio if needed
                    } else {
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Open the gallery to select an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Logout the current user and redirect to the login screen.
     */
    private void logout() {
        mAuth.signOut();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Save the user profile information to Firestore.
     */
    private void saveUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nameField.getText().toString());
        updates.put("email", emailField.getText().toString());
        updates.put("phone", phoneField.getText().toString());
        updates.put("notificationPreferences", bioField.getText().toString());

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Show a dialog to confirm the deletion of the account.
     */
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.\n\nAll your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Delete the current user's account from Firestore and Firebase Auth.
     */
    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // Show progress
        Toast.makeText(requireContext(), "Deleting account...", Toast.LENGTH_SHORT).show();

        // Step 1: Delete user data from Firestore
        FDatabase.getInstance().getDb()
                .collection("users")
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Step 2: Delete Firebase Auth account
                    user.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(requireContext(),
                                        "Account deleted successfully",
                                        Toast.LENGTH_LONG).show();

                                // Redirect to login
                                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                requireActivity().finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(),
                                        "Failed to delete account: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to delete user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}