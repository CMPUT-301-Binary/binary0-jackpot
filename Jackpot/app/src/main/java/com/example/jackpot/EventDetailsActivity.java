package com.example.jackpot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: EventDetailsActivity.java
 *
 * Purpose/Role:
 *   Detail screen for a single Event. Displays event metadata (name, date, price, capacity,
 *   category, registration window, poster image) and exposes role-specific actions:
 *   - Entrant: join waiting list
 *   - Organizer: update poster image
 *   - Admin: delete event
 *
 * Design Notes:
 *   - View/Controller layer (Android Activity). Business/persistence logic delegated to FDatabase.
 *   - Uses Glide for image loading, Firebase Storage for uploads, and Firestore for posterUri updates.
 *   - Receives initial event data via Intent extras; refreshes full record from Firestore on load.
 *   - Runtime image-picker via Activity Result API; upload continues even if user navigates back.
 *
 * Outstanding Issues / TODOs:
 *   - TODO: Fix back-press UX (currently requires two presses to exit this screen).
 *   - TODO: Add basic error/empty states for missing event or network failures.
 */


/**
 * Activity that presents the details of a single {@link Event} and routes role-specific actions.
 */
public class EventDetailsActivity extends AppCompatActivity {

    // TODO: Fix bug: User should only press back once to go to previous screen. Currently user needs to press back twice.
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

    // added for functionality to update photo
    private ImageView posterImageView;
    private Button updatePhotoBtn;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri pickedImageUri;
    private com.google.firebase.storage.UploadTask currentUpload;

    /**
     * Load the current user from Firestore.
     */
    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FDatabase.getInstance().getDb().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    currentUser = snapshot.toObject(User.class);
                    if (currentUser == null || currentUser.getRole() == null) return;

                    // reset all buttons to hidden first
                    setDefaultVisibility();

                    switch (currentUser.getRole()) {
                        case ADMIN:
                            // Admin-only: show Delete button
                            deleteButton.setVisibility(View.VISIBLE);
                            break;

                        case ORGANIZER:
                            // Organizer-only: show Update Photo button
                            updatePhotoBtn.setVisibility(View.VISIBLE);
                            break;

                        case ENTRANT:
                            // Entrant-only: show Join button
                            joinButton.setVisibility(View.VISIBLE);
                            break;
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user", e));
    }

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}. Otherwise it is null.
     *
     */
    // From OpenAI, ChatGPT (GPT-5 Thinking), "Register image picker, show local Glide preview, and hook Update Photo click", 2025-11-07
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeViews();
        setDefaultVisibility(); // hides all the buttons until we know the role
        loadEventData(); // sets eventId and loads details
        setupButtons();
        loadCurrentUser(); // will show the right button after role is known

        // Register image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            pickedImageUri = uri;
                            Glide.with(EventDetailsActivity.this)
                                    .load(pickedImageUri)
                                    .centerCrop()
                                    .into(posterImageView);

                            final int takeFlags = result.getData().getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                            uploadPosterAndSaveUrl(pickedImageUri);
                        }
                    }
                }
        );

        updatePhotoBtn.setOnClickListener(v -> openImagePicker());

        // From OpenAI, ChatGPT (GPT-5 Thinking), "Back press UX: notify upload continues in background", 2025-11-07
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentUpload != null && currentUpload.isInProgress()) {
                    Toast.makeText(EventDetailsActivity.this, "Uploading in background…", Toast.LENGTH_SHORT).show();
                }
                // let the system handle the back press
                setEnabled(false);
//                EventDetailsActivity.this.onBackPressed();
            }
        });
    }

    /**
     * Initialize the views in the activity.
     */
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

        posterImageView = eventPoster;
        updatePhotoBtn = findViewById(R.id.event_details_update_photo_button);
    }

    /**
     * Load the event data from Firestore.
     */
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

    /**
     * Load the full event from Firestore.
     */
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

    /**
     * Loads and displays the poster image for the current event into {@link #eventPoster}.
     *
     * If {@code posterUri} is non-null and non-empty, the image is fetched with Glide
     * using a placeholder and an error fallback. Otherwise, a default drawable is shown.
     *
     * @param posterUri The URI of the event poster image
     */
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

    /**
     * Display event information in the UI.
     *
     * @param name Name of the event
     * @param description Event description
     * @param location Event location
     * @param category Event category
     * @param price Event price
     * @param capacity Event capacity
     * @param dateMillis Event date
     * @param regOpenMillis Event registration open time
     * @param regCloseMillis Event registration close time
     * @param waitingCount Number of people waiting to join
     */
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

    /**
     * Set up the buttons in the UI.
     */
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

    /**
     * Join the current event's waiting list.
     * @param event The event to join
     */
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
                currentUser.getProfileImageUrl(),
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

    /**
     * Delete the current event.
     * @param id The ID of the event to delete
     */
    private void deleteEvent(String id) {
        FDatabase.getInstance().deleteEvent(eventId, new FDatabase.StatusCallback() {

            /**
             * Callback for when the delete operation succeeds.
             */
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            /**
             * Callback for when the delete operation fails.
             * @param error The error message
             */
            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(EventDetailsActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    // helper functions

    /**
     * Open the image picker.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Upload the selected image to Firebase Storage and update the event's posterUri in Firestore.
     *
     * @param fileUri The URI of the selected image
     */
    // From OpenAI, ChatGPT (GPT-5 Thinking), "Upload poster + Firestore update (posterUri) with background-safe callbacks and Glide reload", 2025-11-07
    private void uploadPosterAndSaveUrl(Uri fileUri) {
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(posterImageView, "Missing event id", Snackbar.LENGTH_LONG).show();
            return;
        }

        // If there’s an existing upload, cancel it before starting a new one
        if (currentUpload != null && currentUpload.isInProgress()) {
            currentUpload.cancel();
        }

        updatePhotoBtn.setEnabled(false);

        String imageName = "posters/" + java.util.UUID.randomUUID() + ".png";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(imageName);

        currentUpload = ref.putFile(fileUri);

        currentUpload
                .addOnSuccessListener(ts -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String posterDownloadUrl = downloadUri.toString();

                    // Always update Firestore, even if Activity is finishing/destroyed.
                    FirebaseFirestore.getInstance()
                            .collection("events")
                            .document(eventId)
                            .update("posterUri", posterDownloadUrl)
                            .addOnSuccessListener(unused -> {
                                // Only touch views if we're still alive
                                boolean alive = !isFinishing() && !(android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed());
                                if (alive) {
                                    Glide.with(this)
                                            .load(posterDownloadUrl)
                                            .centerCrop()
                                            .into(posterImageView);
                                    com.google.android.material.snackbar.Snackbar
                                            .make(posterImageView, "Photo updated", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                                            .show();
                                }
                                updatePhotoBtn.setEnabled(true);
                            })
                            .addOnFailureListener(e -> {
                                boolean alive = !isFinishing() && !(android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed());
                                if (alive) {
                                    com.google.android.material.snackbar.Snackbar
                                            .make(posterImageView, "Saved to storage, but failed to update event: " + e.getMessage(),
                                                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                                            .show();
                                }
                                updatePhotoBtn.setEnabled(true);
                            });
                }))
                .addOnFailureListener(e -> {
                    boolean alive = !isFinishing() && !(android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed());
                    if (alive) {
                        com.google.android.material.snackbar.Snackbar
                                .make(posterImageView, "Upload failed: " + e.getMessage(),
                                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                                .show();
                    }
                    updatePhotoBtn.setEnabled(true);
                });

    }

    /**
     * Hide all the buttons in the UI.
     */
    private void setDefaultVisibility() {
        deleteButton.setVisibility(View.GONE);
        joinButton.setVisibility(View.GONE);
        updatePhotoBtn.setVisibility(View.GONE);
    }
}
