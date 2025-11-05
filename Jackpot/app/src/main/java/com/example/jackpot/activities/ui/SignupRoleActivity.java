package com.example.jackpot.activities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.R;

public class SignupRoleActivity extends AppCompatActivity {
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
