package com.example.jackpot;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.jackpot.ui.image.Image;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Activity that presents the details of a single {@link Event} and routes role-specific actions.
 * Displays a primary poster and a QR code in a ViewPager2.
 */
public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";

    // Text UI
    private TextView eventName;
    private TextView eventDescription;
    private TextView eventCriteria;
    private TextView eventLocation;
    private TextView eventDate;
    private TextView eventPrice;
    private TextView eventCapacity;
    private TextView eventWaiting;
    private TextView eventCategory;
    private TextView eventRegOpen;
    private TextView eventRegClose;

    private Button joinButton;
    private ImageButton backButton;
    private Button deleteButton;
    private Button updatePhotoBtn;

    // ViewPager2 for poster + QR code
    private ViewPager2 eventPager;
    private ImagePagerAdapter imagePagerAdapter;
    private final List<String> pagerImages = new ArrayList<>(); // index 0 = poster, 1 = QR (if exists)

    // Data
    private String eventId;
    private User currentUser;
    private int waitingCount;
    private Event currentEvent;

    // Image picker / upload
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri pickedImageUri;
    private com.google.firebase.storage.UploadTask currentUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeViews();
        setDefaultVisibility(); // hide role-based buttons until we know the role
        loadEventData();        // sets eventId + basic info from intent
        setupButtons();
        // Load current user AFTER eventId is set
        loadCurrentUser();      // shows the proper button based on role

        // Register image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            pickedImageUri = uri;
                            // Just upload; the pager will refresh once Firestore is updated
                            uploadPosterAndSaveUrl(pickedImageUri);
                        }
                    }
                }
        );

        updatePhotoBtn.setOnClickListener(v -> openImagePicker());

        // Back press UX (notify if upload in progress, then go back once)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentUpload != null && currentUpload.isInProgress()) {
                    Toast.makeText(EventDetailsActivity.this,
                            "Uploading in background…", Toast.LENGTH_SHORT).show();
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload the event data when activity resumes to catch any changes
        if (eventId != null) {
            loadFullEventFromDatabase();
        }
    }

    /**
     * Initialize all views.
     */
    private void initializeViews() {
        deleteButton = findViewById(R.id.event_details_delete_button);
        eventName = findViewById(R.id.event_details_name);
        eventDescription = findViewById(R.id.event_details_description);
        eventCriteria = findViewById(R.id.event_details_criteria);
        eventLocation = findViewById(R.id.event_details_location);
        eventDate = findViewById(R.id.event_details_date);
        eventPrice = findViewById(R.id.event_details_price);
        eventCapacity = findViewById(R.id.event_details_capacity);
        eventWaiting = findViewById(R.id.event_details_waiting);
        eventCategory = findViewById(R.id.event_details_category);
        eventRegOpen = findViewById(R.id.event_details_reg_open);
        eventRegClose = findViewById(R.id.event_details_reg_close);
        joinButton = findViewById(R.id.event_details_join_button);
        backButton = findViewById(R.id.event_details_back_button);
        updatePhotoBtn = findViewById(R.id.event_details_update_photo_button);

        // ViewPager2
        eventPager = findViewById(R.id.event_details_pager);
    }

    /**
     * Hide all role-based buttons initially.
     */
    private void setDefaultVisibility() {
        deleteButton.setVisibility(View.GONE);
        joinButton.setVisibility(View.GONE);
        updatePhotoBtn.setVisibility(View.GONE);
    }

    /**
     * Load the currently authenticated user and show the correct button.
     */
    private void loadCurrentUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.w(TAG, "No authenticated user; role-based buttons remain hidden.");
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FDatabase.getInstance().getDb().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    currentUser = snapshot.toObject(User.class);
                    if (currentUser == null || currentUser.getRole() == null) return;

                    setDefaultVisibility();

                    switch (currentUser.getRole()) {
                        case ADMIN:
                            deleteButton.setVisibility(View.VISIBLE);
                            // Join button remains hidden for admins
                            break;

                        case ORGANIZER:
                            // Only show update photo button if organizer owns this event
                            checkEventOwnershipAndShowButton();
                            // Join button remains hidden for organizers
                            break;

                        case ENTRANT:
                            joinButton.setVisibility(View.VISIBLE);
                            // Check button state immediately after making it visible
                            updateJoinButtonState();
                            break;
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user", e));
    }

    /**
     * Check if the current organizer owns this event and show update photo button accordingly.
     */
    private void checkEventOwnershipAndShowButton() {
        if (eventId == null || currentUser == null) {
            Log.w(TAG, "Cannot check ownership - eventId: " + eventId + ", currentUser: " + currentUser);
            return;
        }

        Log.d(TAG, "Checking event ownership for eventId: " + eventId + ", userId: " + currentUser.getId());

        FDatabase.getInstance().getDb().collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.w(TAG, "Event document does not exist");
                        return;
                    }

                    // Try to get the creator/organizer ID from the document
                    // The field is called "createdBy" in Firestore
                    String eventCreatorId = documentSnapshot.getString("createdBy");

                    Log.d(TAG, "Event createdBy: " + eventCreatorId + ", Current userId: " + currentUser.getId());

                    if (eventCreatorId != null && eventCreatorId.equals(currentUser.getId())) {
                        Log.d(TAG, "User IS the organizer - showing update photo button");
                        updatePhotoBtn.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "User is NOT the organizer - hiding update photo button");
                        updatePhotoBtn.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check event ownership", e);
                    updatePhotoBtn.setVisibility(View.GONE);
                });
    }

    /**
     * Read basic event info from Intent and then fetch full event from DB.
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
        String criteria = getIntent().getStringExtra("EVENT_CRITERIA");
        String location = getIntent().getStringExtra("EVENT_LOCATION");
        String category = getIntent().getStringExtra("EVENT_CATEGORY");
        Double price = getIntent().hasExtra("EVENT_PRICE")
                ? getIntent().getDoubleExtra("EVENT_PRICE", 0.0)
                : null;
        int capacity = getIntent().getIntExtra("EVENT_CAPACITY", 0);
        long dateMillis = getIntent().getLongExtra("EVENT_DATE", 0L);
        long regOpenMillis = getIntent().getLongExtra("EVENT_REG_OPEN", 0L);
        long regCloseMillis = getIntent().getLongExtra("EVENT_REG_CLOSE", 0L);
        waitingCount = getIntent().getIntExtra("EVENT_WAITING_COUNT", 0);

        displayEventInfo(name, description, criteria, location, category, price, capacity,
                dateMillis, regOpenMillis, regCloseMillis, waitingCount);

        loadFullEventFromDatabase();
    }


    private boolean isUserInWaitingList(Event event) {
        if (currentUser == null || event.getWaitingList() == null) {
            return false;
        }

        ArrayList<User> users = event.getWaitingList().getUsers();
        if (users == null) {
            return false;
        }

        for (User user : users) {
            if (user != null && user.getId() != null && user.getId().equals(currentUser.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the join button state based on whether user is in waiting list
     */
    private void updateJoinButtonState() {
        if (currentEvent != null && currentUser != null && currentUser.getRole() == User.Role.ENTRANT) {
            if (isUserInWaitingList(currentEvent)) {
                joinButton.setEnabled(false);
                joinButton.setText("Joined");
            } else {
                joinButton.setEnabled(true);
                joinButton.setText("Join Waiting List");
            }
        }
    }

    /**
     * Fetch full event from Firestore (including waitingList, posterUri, qrCodeId).
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

                    if (event.getCriteria() != null && !event.getCriteria().isEmpty()) {
                        eventCriteria.setText(event.getCriteria());
                    }

                    // Update join button state
                    updateJoinButtonState();

                    // Load poster + QR code into ViewPager2
                    loadEventImages(event);
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load event from database: " + error);
                runOnUiThread(() -> {
                    joinButton.setEnabled(false);
                    joinButton.setText("Unable to join");
                    Snackbar.make(eventPager, "Failed to load event details",
                            Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Populate ViewPager2 with:
     *  - Index 0: Poster image (event.posterUri, if present)
     *  - Index 1: QR code image (from 'images' collection, if present)
     */
    private void loadEventImages(Event event) {
        pagerImages.clear();
        if (event == null) {
            setupOrRefreshPager();
            return;
        }
        // Poster at index 0
        String posterUri = event.getPosterUri();
        if (posterUri == null || posterUri.isEmpty() || posterUri.equals("default")) {
            // Load placeholder for deleted/missing poster
            pagerImages.add("default");
        } else {
            pagerImages.add(posterUri);
        }
        // QR code at index 1 (lookup via QR Code ID)
        String qrCodeId = event.getQrCodeId();
        if (qrCodeId == null || qrCodeId.isEmpty()) {
            // No QR code — finish with only poster image
            setupOrRefreshPager();
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("images")
                .whereEqualTo("imageType", Image.TYPE_QR_CODE)
                .whereEqualTo("imageID", qrCodeId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Image img = querySnapshot.getDocuments()
                                .get(0)
                                .toObject(Image.class);
                        if (img != null) {
                            String qrUrl = img.getImageUrl();
                            if (qrUrl == null || qrUrl.isEmpty() || qrUrl.equals("default")) {
                                pagerImages.add("default"); // default placeholder image
                            } else {
                                pagerImages.add(qrUrl);
                            }
                        } else {
                            pagerImages.add("default");
                        }
                    } else {
                        // QR entry missing from Firestore
                        pagerImages.add("default");
                    }
                    setupOrRefreshPager();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load QR image metadata", e);
                    pagerImages.add("default");
                    setupOrRefreshPager();
                });
    }

    /**
     * Attach or refresh the ViewPager2 adapter.
     */
    private void setupOrRefreshPager() {
        if (imagePagerAdapter == null) {
            imagePagerAdapter = new ImagePagerAdapter(this, pagerImages);
            eventPager.setAdapter(imagePagerAdapter);
        } else {
            imagePagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Display basic event info in the UI.
     */
    private void displayEventInfo(String name, String description, String criteria, String location,
                                  String category, Double price, int capacity,
                                  long dateMillis, long regOpenMillis, long regCloseMillis,
                                  int waitingCount) {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        eventName.setText(name != null ? name : "Event Name");
        eventDescription.setText(
                description != null && !description.isEmpty()
                        ? description
                        : "No description available");
        eventCriteria.setText(
                criteria != null && !criteria.isEmpty()
                        ? criteria
                        : "No criteria available");
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

        eventCapacity.setText(String.format(Locale.getDefault(),
                "%d spots available", capacity));
        eventWaiting.setText(String.format(Locale.getDefault(),
                "%d people waiting", waitingCount));

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
     * Wire up buttons: back, delete, join.
     */
    private void setupButtons() {
        backButton.setOnClickListener(v -> finish());

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
     * Entrant joins waiting list.
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
                currentUser.getDevice(),
                currentUser.getGeoPoint()
        );

        if (event.hasEntrant(entrant.getId())) {
            Toast.makeText(this, "You are already in this event", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            entrant.joinWaitingList(event);

            // Update the database
            FDatabase.getInstance().updateEvent(event);

            // Update the waiting count
            waitingCount++;
            eventWaiting.setText(String.format(Locale.getDefault(),
                    "%d people waiting", waitingCount));

            // Update button state immediately
            joinButton.setEnabled(false);
            joinButton.setText("Joined");

            // Update the current event reference
            currentEvent = event;

            Toast.makeText(this, "Joined waiting list!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error joining waiting list", e);
        }
    }

    /**
     * Delete the current event.
     */
    private void deleteEvent(String id) {
        FDatabase.getInstance().deleteEvent(id, new FDatabase.StatusCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EventDetailsActivity.this,
                            "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(EventDetailsActivity.this,
                                "Delete failed: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Launch image picker for poster update.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Upload selected image to Storage and update event.posterUri, then refresh the pager.
     */
    private void uploadPosterAndSaveUrl(Uri fileUri) {
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(eventPager, "Missing event id", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Cancel any previous upload
        if (currentUpload != null && currentUpload.isInProgress()) {
            currentUpload.cancel();
        }

        updatePhotoBtn.setEnabled(false);

        String imageName = "posters/" + UUID.randomUUID() + ".png";
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child(imageName);

        currentUpload = ref.putFile(fileUri);

        currentUpload
                .addOnSuccessListener(ts -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String posterDownloadUrl = downloadUri.toString();

                    FirebaseFirestore.getInstance()
                            .collection("events")
                            .document(eventId)
                            .update("posterUri", posterDownloadUrl)
                            .addOnSuccessListener(unused -> {
                                // Update local model
                                if (currentEvent != null) {
                                    currentEvent.setPosterUri(posterDownloadUrl);
                                }

                                // Update pagerImages[0] = posterDownloadUrl
                                if (pagerImages.isEmpty()) {
                                    pagerImages.add(posterDownloadUrl);
                                } else {
                                    pagerImages.set(0, posterDownloadUrl);
                                }
                                setupOrRefreshPager();

                                Snackbar.make(eventPager, "Photo updated",
                                        Snackbar.LENGTH_LONG).show();
                                updatePhotoBtn.setEnabled(true);
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(eventPager,
                                                "Saved to storage, but failed to update event: " + e.getMessage(),
                                                Snackbar.LENGTH_LONG)
                                        .show();
                                updatePhotoBtn.setEnabled(true);
                            });
                }))
                .addOnFailureListener(e -> {
                    boolean alive = !isFinishing() && !(Build.VERSION.SDK_INT >= 17 && isDestroyed());
                    if (alive) {
                        Snackbar.make(eventPager,
                                        "Upload failed: " + e.getMessage(),
                                        Snackbar.LENGTH_LONG)
                                .show();
                    }
                    updatePhotoBtn.setEnabled(true);
                });
    }
}