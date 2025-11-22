package com.example.jackpot.ui.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.jackpot.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

/**
 * The fragment which prompts the user to allow location access to their device.
 */
public class LocationFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private SwitchMaterial locationSwitch;

    private FirebaseFirestore db;
    private String uid;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean locationEnabled = false; // True if user has real GPS saved

    /**
     * Called to have the fragment instantiate its user interface view.
     * Triggers a pop-up window for the user to select whether or not they want to allow location shared from their device.
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
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_location, container, false);

        locationSwitch = root.findViewById(R.id.switch_location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCurrentLocationStatus();

        locationSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                enableLocation();
            } else {
                disableLocation();
            }
        });

        return root;
    }

    // Load current Firestore (0,0)
    private void loadCurrentLocationStatus() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        GeoPoint gp = doc.getGeoPoint("geoPoint");

                        if (gp == null || (gp.getLatitude() == 0.0 && gp.getLongitude() == 0.0)) {
                            locationEnabled = false;
                        } else {
                            locationEnabled = true;
                        }

                        locationSwitch.setChecked(locationEnabled);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load location", Toast.LENGTH_SHORT).show());
    }

    // Enable Location
    private void enableLocation() {

        // Check permission
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );

            // Revert switch temporarily
            locationSwitch.setChecked(false);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location == null) {
                Toast.makeText(requireContext(), "Unable to access location", Toast.LENGTH_SHORT).show();
                locationSwitch.setChecked(false);
                return;
            }

            GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());

            db.collection("users").document(uid)
                    .update("geoPoint", gp)
                    .addOnSuccessListener(v -> {
                        locationEnabled = true;
                        Toast.makeText(requireContext(), "Location enabled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        locationSwitch.setChecked(false);
                        Toast.makeText(requireContext(), "Error enabling location", Toast.LENGTH_SHORT).show();
                    });

        });
    }

    // Disable Location (set to 0,0)
    private void disableLocation() {
        GeoPoint disabled = new GeoPoint(0.0, 0.0);

        db.collection("users").document(uid)
                .update("geoPoint", disabled)
                .addOnSuccessListener(v -> {
                    locationEnabled = false;
                    Toast.makeText(requireContext(), "Location disabled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    locationSwitch.setChecked(true);
                    Toast.makeText(requireContext(), "Error disabling location", Toast.LENGTH_SHORT).show();
                });
    }
}

