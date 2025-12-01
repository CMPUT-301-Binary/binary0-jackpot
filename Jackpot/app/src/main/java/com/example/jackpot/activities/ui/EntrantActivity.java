package com.example.jackpot.activities.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jackpot.Event;
import com.example.jackpot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
/**
 * The activity for the entrant pages.
 * The entrants have general permission to view and join events..
 * Todo: Find a way to integrate this class with the current login system.
 */
public class EntrantActivity extends AppCompatActivity {
    private DatabaseReference db;

    /**
     * Lifecycle entry point. Currently a stub; UI wiring is not active.
     * @param savedInstanceState state bundle from Android lifecycle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_entrant);

    }
}
