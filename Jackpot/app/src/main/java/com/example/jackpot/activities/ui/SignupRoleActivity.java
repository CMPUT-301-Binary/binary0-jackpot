package com.example.jackpot.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.R;

/**
 * The activity which allows the user to choose to sign up as an organizer or an entrant.
 * This activity is shown when the user presses "sign-in" if they enter the app while logged out.
 */
public class SignupRoleActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_signup_role);

        Button organizerBtn = findViewById(R.id.organizerbtn);
        Button entrantBtn = findViewById(R.id.entrantbtn);

        organizerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SignupOrganizerActivity.class)));

        entrantBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SignupEntrantActivity.class)));
    }
}
