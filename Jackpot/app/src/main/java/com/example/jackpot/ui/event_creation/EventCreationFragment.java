package com.example.jackpot.ui.event_creation;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jackpot.Event;
import com.example.jackpot.Image;
import com.example.jackpot.R;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventCreationFragment extends Fragment {

    private Button selectPhotoButton;
    private TextView selectedPhotoTextView;
    private EditText editTextEventName;
    private EditText editTextEventDescription;
    private EditText editTextEventLocation;
    private EditText editTextEventDate;
    private EditText editTextEventTime;
    private EditText editTextEventCapacity;
    private EditText editTextEventPrice;
    private CheckBox geoLocationBox;
    private CheckBox qrCodeBox;
    private Button submitButton;
    private FirebaseFirestore db;

    private Uri selectedImageUri;

    public EventCreationFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDescription = view.findViewById(R.id.editDescription);
        editTextEventLocation = view.findViewById(R.id.editAddress);
        editTextEventDate = view.findViewById(R.id.editDate);
        editTextEventTime = view.findViewById(R.id.editTime);
        editTextEventCapacity = view.findViewById(R.id.editCapacity);
        editTextEventPrice = view.findViewById(R.id.editTextPrice);
        geoLocationBox = view.findViewById(R.id.geoLocationBox);
        qrCodeBox = view.findViewById(R.id.qrCodeBox);
        submitButton = view.findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(v -> {
            createEvent();
        });
        db = FirebaseFirestore.getInstance();



        super.onViewCreated(view, savedInstanceState);
        selectedPhotoTextView = view.findViewById(R.id.selectedPhotoText);

        // Registers a photo picker activity launcher in single-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        selectedPhotoTextView.setText(uri.toString());
                        selectedImageUri = uri;
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        selectPhotoButton = view.findViewById(R.id.uploadImageButton);
        //THIS IS TO BE MODIFIED IF THE FAB BUTTON'S ID HAS CHANGED
        selectPhotoButton.setOnClickListener(v -> {
            //If the button is clicked, open up the photo picker
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());


        });

    }
    public void createEvent() {
        // Gather form data
        String eventName = editTextEventName.getText().toString().trim();
        String eventDescription = editTextEventDescription.getText().toString().trim();
        String eventLocation = editTextEventLocation.getText().toString().trim();
        String eventDate = editTextEventDate.getText().toString().trim();
        String eventTime = editTextEventTime.getText().toString().trim();
        String capacityStr = editTextEventCapacity.getText().toString().trim();
        String priceStr = editTextEventPrice.getText().toString().trim();
        boolean geoLocation = geoLocationBox.isChecked();
        boolean qrCode = qrCodeBox.isChecked();

        // Basic validation
        // TODO: validate all other inputs
        // TODO: use appropriate pickers
        if (eventName.isEmpty()) {
            Toast.makeText(requireContext(), "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity = 0;
        try { capacity = capacityStr.isEmpty() ? 0 : Integer.parseInt(capacityStr); } catch (NumberFormatException ignored) {}

        double price = 0.0;
        try { price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr); } catch (NumberFormatException ignored) {}

        // Current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : null;

        String eventId = UUID.randomUUID().toString();

        // Poster image
        String posterUri = (selectedImageUri != null) ? selectedImageUri.toString() : null;

        // Build the event payload
        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("eventId", eventId);
        eventDoc.put("name", eventName);
        eventDoc.put("description", eventDescription);
        eventDoc.put("location", eventLocation);
        eventDoc.put("date", eventDate);
        eventDoc.put("time", eventTime);
        eventDoc.put("capacity", capacity);
        eventDoc.put("price", price);
        eventDoc.put("geoLocation", geoLocation);
        eventDoc.put("qrCode", qrCode);
        eventDoc.put("posterUri", posterUri);
        eventDoc.put("createdBy", userId);
        eventDoc.put("createdAt", FieldValue.serverTimestamp());

        // Write to Firestore - collection "events" with ID
        db.collection("events")
                .document(eventId)
                .set(eventDoc)
                .addOnSuccessListener(v -> {
                    Toast.makeText(requireContext(), "Event created!", Toast.LENGTH_SHORT).show();
                    // TODO: optionally navigate back or clear the form
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to create event", e);
                    Toast.makeText(requireContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}