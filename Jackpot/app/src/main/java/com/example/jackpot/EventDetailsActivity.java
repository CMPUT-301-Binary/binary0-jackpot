package com.example.jackpot;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private String eventId;
    private User currentUser;
    private int waitingCount;
    private Event currentEvent; // Store the event for later use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeViews();
        loadEventData();
        setupButtons();
    }

    private void initializeViews() {
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
        // Get event data from intent
        eventId = getIntent().getStringExtra("EVENT_ID");

        Log.d(TAG, "Loading event with ID: " + eventId);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display the data that was passed via Intent immediately
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

        // Load the full event object from database in the background for joining
        loadFullEventFromDatabase();
    }

    private void loadFullEventFromDatabase() {
        // Check if FDatabase has the getEventById method
        FDatabase db = FDatabase.getInstance();

        // Try to get event from database
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
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load event from database: " + error);
                // Don't show error to user - they can still see the event info
                // Just disable the join button
                runOnUiThread(() -> {
                    joinButton.setEnabled(false);
                    joinButton.setText("Unable to join");
                });
            }
        });
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

        joinButton.setOnClickListener(v -> {
            if (currentEvent == null) {
                Toast.makeText(this, "Event is still loading, please try again", Toast.LENGTH_SHORT).show();
                return;
            }
            joinWaitingList(currentEvent);
        });
    }

    private void joinWaitingList(Event event) {
        // TODO: Get current user from your session/database
        // For now, you'll need to implement getting the current logged-in user
        // currentUser = SessionManager.getCurrentUser();
        // or
        // currentUser = getIntent().getParcelableExtra("CURRENT_USER");

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

        try {
            entrant.joinWaitingList(event);
            FDatabase.getInstance().updateEvent(event);
            Toast.makeText(this, "Successfully joined waiting list!", Toast.LENGTH_SHORT).show();

            // Update the waiting count display
            waitingCount++;
            eventWaiting.setText(String.format(Locale.getDefault(), "%d people waiting", waitingCount));

            joinButton.setEnabled(false);
            joinButton.setText("Joined");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}