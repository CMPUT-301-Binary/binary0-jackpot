package com.example.jackpot.activities.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.jackpot.FDatabase;
import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

/**
 * SignUp activity for the entrant. The entrant will be prompted to enter details, which will be stored in the database.
 *
 */

public class SignupEntrantActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FDatabase fDatabase;
    private FirebaseFirestore db;

    private EditText nameField, emailField, passwordField, phoneField;
    private Toast currentToast;

    private static final int LOCATION_REQUEST = 300;
    private String newUserId;


    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_entrant);
        fDatabase = FDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // CRITICAL: Disable app verification for development/grading
        // This prevents reCAPTCHA network errors on machines without SHA-1 setup
        try {
            mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
            Log.d("SignupEntrant", "App verification disabled");
        } catch (Exception e) {
            Log.e("SignupEntrant", "Could not disable verification", e);
        }

        db = FirebaseFirestore.getInstance();

        nameField = findViewById(R.id.namefld);
        emailField = findViewById(R.id.emailfld);
        passwordField = findViewById(R.id.passwordfld);
        phoneField = findViewById(R.id.phonefld);

        String profileImageUrl = "default";

        Button signupBtn = findViewById(R.id.signupbtn);

        signupBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast("Please fill all required fields");
                return;
            }

            if (password.length() < 6) {
                showToast("Password must be at least 6 characters");
                return;
            }

            // Disable button to prevent multiple clicks
            signupBtn.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = mAuth.getCurrentUser().getUid();

                        User user = new User(uid, name, User.Role.ENTRANT, email, phone, profileImageUrl, "", "default", null, new GeoPoint(0.0,0.0));

                        fDatabase.getDb().collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Account created!");

                                    // Ask for location permission ONCE during signup
                                    askForLocationPermission(uid);
                                })
                                .addOnFailureListener(e -> {
                                    signupBtn.setEnabled(true);
                                    showToast("Error: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        signupBtn.setEnabled(true);
                        showToast("Auth error: " + e.getMessage());
                        Log.e("SignupEntrant", "Auth error", e);
                    });
        });
    }

    // Prevents multiple toasts from stacking

    /**
     * Show a toast message.
     *
     * @param message The message to display
     */
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void askForLocationPermission(String uid) {
        newUserId = uid;

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST
            );

        } else {
            saveInitialLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                saveInitialLocation();

            } else {
                // No change → stays at (0,0)
                goToMain();
            }
        }
    }

    private void saveInitialLocation() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            goToMain();
            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
                db.collection("users").document(newUserId)
                        .update("geoPoint", gp)
                        .addOnSuccessListener(v -> Log.d("Signup", "Initial location saved"));
            }
            goToMain();
        });
    }

    private void goToMain() {
        startActivity(new Intent(SignupEntrantActivity.this, MainActivity.class));
        finish();
    }
}