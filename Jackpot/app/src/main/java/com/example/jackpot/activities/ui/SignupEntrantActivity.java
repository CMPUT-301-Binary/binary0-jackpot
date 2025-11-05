package com.example.jackpot.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupEntrantActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText nameField, emailField, passwordField, phoneField;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_entrant);

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

                        User user = new User(uid, name, User.Role.ENTRANT, email, phone, password, "default", null);

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Entrant account created!");
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    signupBtn.setEnabled(true);
                                    showToast("Firestore error: " + e.getMessage());
                                    Log.e("SignupEntrant", "Firestore error", e);
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
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}