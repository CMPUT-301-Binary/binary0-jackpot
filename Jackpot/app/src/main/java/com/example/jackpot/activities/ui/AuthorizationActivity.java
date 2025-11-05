package com.example.jackpot.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.FDatabase;
import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class AuthorizationActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText emailField, passwordField, nameField;
    FDatabase fDatabase = FDatabase.getInstance();

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            // Already logged in
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        emailField = findViewById(R.id.emailfld);
        passwordField = findViewById(R.id.passwordfld);
        nameField = findViewById(R.id.namefld);
        Button signupButton = findViewById(R.id.signupbtn);
        Button loginButton = findViewById(R.id.loginbtn);

        signupButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String name = nameField.getText().toString().trim();
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                fDatabase.getDb().collection("users").document(uid).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                User user = doc.toObject(User.class);
                                if (user != null) {
                                    Log.d("Firestore", "User name: " + user.getName());
                                } else {
                                    Log.e("Firestore", "User object is null");
                                }
                            } else {
                                Log.e("Firestore", "User document does not exist");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Failed to get user: " + e.getMessage());
                        });
            }

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String firebaseUid = mAuth.getCurrentUser().getUid();
                        // FIXED: Use Firebase UID as the user ID (removed UUID generation)

                        // Create User object
                        User user = new User(
                                firebaseUid,
                                name,
                                User.Role.ENTRANT,
                                email,
                                "",
                                "",
                                "default",
                                null
                        );

                        // Save user in Firestore using Firebase UID as doc ID
                        fDatabase.getDb().collection("users").document(firebaseUid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });


    }
}