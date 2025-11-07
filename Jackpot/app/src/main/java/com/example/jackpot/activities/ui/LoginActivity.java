package com.example.jackpot.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * The activity for the login page.
 * The user can login with their email and password.
 * Upon successful login, the user is then redirected to the home page.
 */

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;


    /**
     * Checks if the user is already logged in.
     * If so, the user is redirected to the home page.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /**
     * Called when the activity is first created.
     * Sets up the login page.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.emailfld);
        passwordField = findViewById(R.id.passwordfld);
        Button loginBtn = findViewById(R.id.loginbtn);
        Button signupBtn = findViewById(R.id.signupbtn);

        loginBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        signupBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SignupRoleActivity.class)));
    }
}
