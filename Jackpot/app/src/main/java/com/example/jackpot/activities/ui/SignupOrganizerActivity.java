package com.example.jackpot.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.FDatabase;
import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SignUp activity for the organizer. The organizer will be prompted to enter details, which will be stored in the database.
 *
 */

public class SignupOrganizerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText nameField, emailField, passwordField, phoneField;
    private Toast currentToast;

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
        FDatabase fDatabase = FDatabase.getInstance();
        setContentView(R.layout.activity_signup_organizer);

        mAuth = FirebaseAuth.getInstance();
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

            // Disable button to prevent double-clicks
            signupBtn.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = mAuth.getCurrentUser().getUid();

                        User user = new User(uid, name, User.Role.ORGANIZER, email, phone, "default","", "default", null);

                        fDatabase.getDb().collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Account created!");
                                    startActivity(new Intent(SignupOrganizerActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    signupBtn.setEnabled(true);
                                    showToast("Error: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Re-enable button if authentication fails
                        signupBtn.setEnabled(true);
                        showToast("Authentication failed: " + e.getMessage());
                    });
        });
    }

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
}