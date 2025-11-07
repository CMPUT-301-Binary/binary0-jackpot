package com.example.jackpot;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";

    private TextView eventName;
    private TextView eventDescription;
    private TextView eventLocation;
    private TextView eventDate;
    private TextView eventPrice;
    private TextView eventCapacity;
    private TextView eventWaiting;
    private TextView eventCategory;
    private TextView eventRegOpen;
    private TextView eventRegClose;
    private ImageView eventPoster;
    private Button joinButton;
    private ImageButton backButton;
    private Button deleteButton;

    private String eventId;
    private User currentUser;
    private int waitingCount;
    private Event currentEvent;


    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FDatabase.getInstance().getDb().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        currentUser = snapshot.toObject(User.class);

                        if (currentUser.getRole() == User.Role.ADMIN) {
                            deleteButton.setVisibility(View.VISIBLE);
                            joinButton.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user", e));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeViews();
        loadEventData();
        setupButtons();
        loadCurrentUser();
    }

    private void initializeViews() {
        deleteButton = findViewById(R.id.event_details_delete_button);
        eventName = findViewById(R.id.event_details_name);
        eventDescription = findViewById(R.id.event_details_description);
        eventLocation = findViewById(R.id.event_details_location);
        eventDate = findViewById(R.id.event_details_date);
        eventPrice = findViewById(R.id.event_details_price);
        eventCapacity = findViewById(R.id.event_details_capacity);
        eventWaiting = findViewById(R.id.event_details_waiting);
        eventCategory = findViewById(R.id.event_details_category);
        eventRegOpen = findViewById(R.id.event_details_reg_open);
        eventRegClose = findViewById(R.id.event_details_reg_close);
        eventPoster = findViewById(R.id.event_details_poster);
        joinButton = findViewById(R.id.event_details_join_button);
        backButton = findViewById(R.id.event_details_back_button);
    }

    private void loadEventData() {
        eventId = getIntent().getStringExtra("EVENT_ID");

        Log.d(TAG, "Loading event with ID: " + eventId);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = getIntent().getStringExtra("EVENT_NAME");
        String description = getIntent().getStringExtra("EVENT_DESCRIPTION");
        String location = getIntent().getStringExtra("EVENT_LOCATION");
        String category = getIntent().getStringExtra("EVENT_CATEGORY");
        Double price = getIntent().hasExtra("EVENT_PRICE") ? getIntent().getDoubleExtra("EVENT_PRICE", 0.0) : null;
        int capacity = getIntent().getIntExtra("EVENT_CAPACITY", 0);
        long dateMillis = getIntent().getLongExtra("EVENT_DATE", 0L);
        long regOpenMillis = getIntent().getLongExtra("EVENT_REG_OPEN", 0L);
        long regCloseMillis = getIntent().getLongExtra("EVENT_REG_CLOSE", 0L);
        waitingCount = getIntent().getIntExtra("EVENT_WAITING_COUNT", 0);

        displayEventInfo(name, description, location, category, price, capacity,
                dateMillis, regOpenMillis, regCloseMillis, waitingCount);

        loadFullEventFromDatabase();
    }

    private void loadFullEventFromDatabase() {
        FDatabase db = FDatabase.getInstance();

        db.getEventById(eventId, new FDatabase.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                Log.d(TAG, "Successfully loaded event from database");
                currentEvent = event;
                runOnUiThread(() -> {
                    // Update waiting count in case it changed
                    if (event.getWaitingList() != null) {
                        waitingCount = event.getWaitingList().size();
                        eventWaiting.setText(String.format(Locale.getDefault(),
                                "%d people waiting", waitingCount));
                    }

                    // Load the event poster image
                    loadEventPoster(event.getPosterUri());
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load event from database: " + error);
                runOnUiThread(() -> {
                    joinButton.setEnabled(false);
                    joinButton.setText("Unable to join");
                });
            }
        });
    }

    private void loadEventPoster(String posterUri) {
        if (posterUri != null && !posterUri.isEmpty()) {
            Log.d(TAG, "Loading poster image: " + posterUri);
            Glide.with(this)
                    .load(posterUri)
                    .placeholder(R.drawable._ukj7h)
                    .error(R.drawable.jackpottitletext)
                    .into(eventPoster);
        } else {
            Log.d(TAG, "No poster URI available, using default image");
            eventPoster.setImageResource(R.drawable._ukj7h);
        }
    }

    private void displayEventInfo(String name, String description, String location,
                                  String category, Double price, int capacity,
                                  long dateMillis, long regOpenMillis, long regCloseMillis,
                                  int waitingCount) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        eventName.setText(name != null ? name : "Event Name");
        eventDescription.setText(description != null && !description.isEmpty() ? description : "No description available");
        eventLocation.setText(location != null ? location : "Location TBD");
        eventCategory.setText(category != null ? category : "Uncategorized");

        if (dateMillis > 0) {
            eventDate.setText(dateFormat.format(new Date(dateMillis)));
        } else {
            eventDate.setText("Date TBD");
        }

        if (price != null && price > 0) {
            eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", price));
        } else {
            eventPrice.setText("Free");
        }

        eventCapacity.setText(String.format(Locale.getDefault(), "%d spots available", capacity));
        eventWaiting.setText(String.format(Locale.getDefault(), "%d people waiting", waitingCount));

        if (regOpenMillis > 0) {
            eventRegOpen.setText(dateFormat.format(new Date(regOpenMillis)));
        } else {
            eventRegOpen.setText("Open now");
        }

        if (regCloseMillis > 0) {
            eventRegClose.setText(dateFormat.format(new Date(regCloseMillis)));
        } else {
            eventRegClose.setText("Until full");
        }
    }

    private void setupButtons() {
        backButton.setOnClickListener(v -> finish());

        if (currentUser != null && currentUser.getRole() == User.Role.ADMIN) {
            deleteButton.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.GONE);
        }

        deleteButton.setOnClickListener(v -> {
            if (eventId == null) {
                Toast.makeText(this, "Cannot delete: Event ID missing", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to permanently delete this event?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEvent(eventId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        joinButton.setOnClickListener(v -> {
            if (currentEvent == null) {
                Toast.makeText(this, "Event is still loading, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            joinWaitingList(currentEvent);
        });
    }

    private void joinWaitingList(Event event) {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to join events", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getRole() != User.Role.ENTRANT) {
            Toast.makeText(this, "Only entrants can join events", Toast.LENGTH_SHORT).show();
            return;
        }

        Entrant entrant = new Entrant(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getRole(),
                currentUser.getEmail(),
                currentUser.getPhone(),
                currentUser.getPassword(),
                currentUser.getNotificationPreferences(),
                currentUser.getDevice()
        );
        if (event.hasEntrant(entrant)){
            //Make a toast for already in event
            Toast.makeText(this, "You are already in this event", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            entrant.joinWaitingList(event);
            FDatabase.getInstance().updateEvent(event);
            Toast.makeText(this, "Object ID:", Toast.LENGTH_SHORT).show();

            waitingCount++;
            eventWaiting.setText(String.format(Locale.getDefault(), "%d people waiting", waitingCount));

            joinButton.setEnabled(false);
            joinButton.setText("Joined");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void deleteEvent(String id) {
        FDatabase.getInstance().deleteEvent(eventId, new FDatabase.StatusCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(EventDetailsActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}