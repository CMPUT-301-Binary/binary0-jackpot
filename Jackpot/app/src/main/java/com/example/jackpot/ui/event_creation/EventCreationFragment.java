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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
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
    private EditText editWaitingListLimit;
    // UI refs
    private EditText editRegOpenDate, editRegOpenTime, editRegCloseDate, editRegCloseTime;


    // Registration picker state
    private Long regOpenDateUtcMs = null, regCloseDateUtcMs = null;
    private Integer regOpenHour = null, regOpenMinute = null;
    private Integer regCloseHour = null, regCloseMinute = null;

    // for date & time pickers
    private Long selectedDateUtcMs = null;
    private Integer selectedHour = null, selectedMinute = null;


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
        editRegOpenDate  = view.findViewById(R.id.editRegOpenDate);
        editRegOpenTime  = view.findViewById(R.id.editRegOpenTime);
        editRegCloseDate = view.findViewById(R.id.editRegCloseDate);
        editRegCloseTime = view.findViewById(R.id.editRegCloseTime);
        editWaitingListLimit = view.findViewById(R.id.editWaitingListLimit);

        submitButton.setOnClickListener(v -> {
            createEvent();
        });
        db = FirebaseFirestore.getInstance();

        editTextEventDate.setFocusable(false);
        editTextEventDate.setClickable(true);
        editTextEventTime.setFocusable(false);
        editTextEventTime.setClickable(true);
        // just making all of them clickable and not focusable
        for (EditText et : new EditText[]{ editRegOpenDate, editRegOpenTime, editRegCloseDate, editRegCloseTime }) {
            et.setFocusable(false);
            et.setClickable(true);
        }

        editTextEventDate.setOnClickListener(v -> openDatePicker());
        editTextEventTime.setOnClickListener(v -> openTimePicker());
        editRegOpenDate.setOnClickListener(v -> openRegDatePicker(true));
        editRegOpenTime.setOnClickListener(v -> openRegTimePicker(true));
        editRegCloseDate.setOnClickListener(v -> openRegDatePicker(false));
        editRegCloseTime.setOnClickListener(v -> openRegTimePicker(false));

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
        // region Validation

        // region Required text fields (ensure they are entered)
        if (!requireText(editTextEventName, "Required")) return;
        if (!requireText(editTextEventPrice, "Required")) return;
        if (!requireText(editTextEventLocation, "Required")) return;
        if (!requireText(editTextEventDate, "Pick a date")) return;
        if (!requireText(editTextEventTime, "Pick a time")) return;
        if (!requireText(editTextEventDescription, "Required")) return;
        if (!requireText(editTextEventCapacity, "Required")) return;
        // endregion

        // region checking for Numerical fields
        // Price: required, numeric, >= 0
        Double priceVal = parseDoubleOrNull(editTextEventPrice.getText().toString());
        if (priceVal == null) { editTextEventPrice.setError("Enter a number"); editTextEventPrice.requestFocus(); return; }
        if (priceVal < 0)      { editTextEventPrice.setError("Must be ≥ 0");   editTextEventPrice.requestFocus(); return; }

        // Capacity: required, integer, >= 1
        Integer capacityVal = parseIntOrNull(editTextEventCapacity.getText().toString());
        if (capacityVal == null) { editTextEventCapacity.setError("Enter an integer"); editTextEventCapacity.requestFocus(); return; }
        if (capacityVal < 1)     { editTextEventCapacity.setError("Must be ≥ 1");       editTextEventCapacity.requestFocus(); return; }

        // Waiting List Limit: optional, but if entered must be integer >= 0
        Integer waitLimitVal = null;
        String waitStr = editWaitingListLimit.getText().toString().trim();
        if (!waitStr.isEmpty()) {
            waitLimitVal = parseIntOrNull(waitStr);
            if (waitLimitVal == null) { editWaitingListLimit.setError("Enter an integer"); editWaitingListLimit.requestFocus(); return; }
            if (waitLimitVal < 0)     { editWaitingListLimit.setError("Must be ≥ 0");      editWaitingListLimit.requestFocus(); return; }
        } else {
            editWaitingListLimit.setError(null);
        }
        // endregion

        // endregion

        // region Build canonical timestamps from pickers
        com.google.firebase.Timestamp eventTs =
                toTimestamp(selectedDateUtcMs, selectedHour, selectedMinute);
        com.google.firebase.Timestamp regOpenTs =
                toTimestamp(regOpenDateUtcMs, regOpenHour, regOpenMinute);
        com.google.firebase.Timestamp regCloseTs =
                toTimestamp(regCloseDateUtcMs, regCloseHour, regCloseMinute);

        // region Validation for canonical timestamps
        if (eventTs == null) {
            editTextEventDate.setError("Pick event date");
            editTextEventTime.setError("Pick event time");
            editTextEventDate.requestFocus();
            return;
        } else {
            editTextEventDate.setError(null);
            editTextEventTime.setError(null);
        }

        // ensure event is in the future
        if (eventTs.compareTo(com.google.firebase.Timestamp.now()) <= 0) {
            editTextEventDate.setError("Event must be in the future");
            editTextEventTime.setError("Event must be in the future");
            editTextEventDate.requestFocus();
            return;
        }

        // Registration presence
        if (regOpenTs == null) {
            editRegOpenDate.setError("Pick open date");
            editRegOpenTime.setError("Pick open time");
            editRegOpenDate.requestFocus();
            return;
        } else { editRegOpenDate.setError(null); editRegOpenTime.setError(null); }

        if (regCloseTs == null) {
            editRegCloseDate.setError("Pick close date");
            editRegCloseTime.setError("Pick close time");
            editRegCloseDate.requestFocus();
            return;
        } else { editRegCloseDate.setError(null); editRegCloseTime.setError(null); }

        // Registration ordering
        if (regOpenTs.compareTo(regCloseTs) >= 0) {
            editRegCloseDate.setError("Close must be after open");
            editRegCloseTime.setError("Close must be after open");
            editRegCloseDate.requestFocus();
            return;
        }

        // Registration must end before event starts
        if (eventTs.compareTo(regCloseTs) <= 0) {
            editRegCloseDate.setError("Must end before event starts");
            editRegCloseTime.setError("Must end before event starts");
            editRegCloseDate.requestFocus();
            return;
        }
        // endregion

        // endregion

        // region Gather form data
        String eventName = editTextEventName.getText().toString().trim();
        String eventDescription = editTextEventDescription.getText().toString().trim();
        String eventLocation = editTextEventLocation.getText().toString().trim();
        String eventDate = editTextEventDate.getText().toString().trim();
        String eventTime = editTextEventTime.getText().toString().trim();
        String capacityStr = editTextEventCapacity.getText().toString().trim();
        String priceStr = editTextEventPrice.getText().toString().trim();
        boolean geoLocation = geoLocationBox.isChecked();
        boolean qrCode = qrCodeBox.isChecked();
        // endregion

        // Current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : null;

        String eventId = UUID.randomUUID().toString();
        String posterUri = (selectedImageUri != null) ? selectedImageUri.toString() : null; // Poster image URI


        // region Build the event payload
        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("eventId", eventId);
        eventDoc.put("name", eventName);
        eventDoc.put("description", eventDescription);
        eventDoc.put("location", eventLocation);

        eventDoc.put("date", eventDate);
        eventDoc.put("time", eventTime);

        eventDoc.put("eventAt",   eventTs);
        eventDoc.put("regOpenAt",  regOpenTs);
        eventDoc.put("regCloseAt", regCloseTs);

        // Numeric fields as numbers
        eventDoc.put("price", priceVal);
        eventDoc.put("capacity", capacityVal);
        if (waitLimitVal != null) eventDoc.put("waitingListLimit", waitLimitVal);

        eventDoc.put("geoLocation", geoLocation);
        eventDoc.put("qrCode", qrCode);
        eventDoc.put("posterUri", posterUri);
        eventDoc.put("createdBy", userId);
        // MARK: can remove this createdAt field if we don't need it
        eventDoc.put("createdAt", FieldValue.serverTimestamp());

        eventDoc.put("regOpenDate",  editRegOpenDate.getText().toString().trim());
        eventDoc.put("regOpenTime",  editRegOpenTime.getText().toString().trim());
        eventDoc.put("regCloseDate", editRegCloseDate.getText().toString().trim());
        eventDoc.put("regCloseTime", editRegCloseTime.getText().toString().trim());

        // Canonical timestamps for queries/sorting (Future use in this project)
//        eventDoc.put("regOpenAt",  regOpenTs);
//        eventDoc.put("regCloseAt", regCloseTs);
        // endregion

        submitButton.setEnabled(false);
        // Write to Firestore - collection "events" with ID
        db.collection("events")
                .document(eventId)
                .set(eventDoc)
                .addOnSuccessListener(v -> {
                    Toast.makeText(requireContext(), "Event created!", Toast.LENGTH_SHORT).show();
                    // TODO: optionally navigate back or clear the form
                    // currently have it just do the back press function
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    submitButton.setEnabled(true);
                    Log.e("Firestore", "Failed to create event", e);
                    Toast.makeText(requireContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // region Date & Time Pickers (and for Registration Start and End)
    private void openDatePicker() {
        var picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select event date")
                .build();
        picker.addOnPositiveButtonClickListener(utcMs -> {
            selectedDateUtcMs = utcMs;
            editTextEventDate.setText(picker.getHeaderText());
        });
        picker.show(getParentFragmentManager(), "event_date_picker");
    }

    private void openTimePicker() {
        var picker = new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTitleText("Select event time")
                .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                .setHour(selectedHour == null ? 18 : selectedHour)
                .setMinute(selectedMinute == null ? 0 : selectedMinute)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();
            editTextEventTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
        });
        picker.show(getParentFragmentManager(), "event_time_picker");
    }

    private void openRegDatePicker(boolean isOpen) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isOpen ? "Select registration OPEN date" : "Select registration CLOSE date")
                .build();

        picker.addOnPositiveButtonClickListener(utcMs -> {
            if (isOpen) {
                regOpenDateUtcMs = utcMs;
                editRegOpenDate.setText(picker.getHeaderText());
            } else {
                regCloseDateUtcMs = utcMs;
                editRegCloseDate.setText(picker.getHeaderText());
            }
        });

        picker.show(getParentFragmentManager(), isOpen ? "reg_open_date" : "reg_close_date");
    }

    private void openRegTimePicker(boolean isOpen) {
        int defHour   = isOpen ? (regOpenHour  == null ? 9  : regOpenHour)  : (regCloseHour  == null ? 17 : regCloseHour);
        int defMinute = isOpen ? (regOpenMinute== null ? 0  : regOpenMinute): (regCloseMinute== null ? 0  : regCloseMinute);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTitleText(isOpen ? "Select registration OPEN time" : "Select registration CLOSE time")
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(defHour)
                .setMinute(defMinute)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            if (isOpen) {
                regOpenHour = picker.getHour();
                regOpenMinute = picker.getMinute();
                editRegOpenTime.setText(String.format("%02d:%02d", regOpenHour, regOpenMinute));
            } else {
                regCloseHour = picker.getHour();
                regCloseMinute = picker.getMinute();
                editRegCloseTime.setText(String.format("%02d:%02d", regCloseHour, regCloseMinute));
            }
        });

        picker.show(getParentFragmentManager(), isOpen ? "reg_open_time" : "reg_close_time");
    }
    // endregion

    private boolean requireText(EditText et, String msg) {
        if (et.getText().toString().trim().isEmpty()) {
            et.setError(msg);
            et.requestFocus();
            return false;
        }
        et.setError(null);
        return true;
    }

    // region Helper functions
    @Nullable
    private Integer parseIntOrNull(String s) {
        try { return s.trim().isEmpty() ? null : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    @Nullable
    private Double parseDoubleOrNull(String s) {
        try { return s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }


    /**
     * Makes a Firestore Timestamp from a picked date and a picked time.
     *
     * How it works:
     * - The date comes from the MaterialDatePicker (as UTC milliseconds for the selected day).
     * - The time comes from the MaterialTimePicker (hour and minute).
     * - We combine them using the phone’s current time zone to get one exact moment.
     *
     * Why use this:
     * - Firestore Timestamps sort and filter correctly (e.g., upcoming events).
     * - Text strings like "11/05/2025" don’t sort reliably across formats/locales.
     *
     * @param dateUtcMs The selected day in milliseconds (value you get from MaterialDatePicker).
     * @param hour      Hour of day in 24-hour format (0–23) from MaterialTimePicker.
     * @param minute    Minute of the hour (0–59) from MaterialTimePicker.
     * @return A Firestore Timestamp for the combined date and time,
     *      or {@code null} if any input is missing.
     */
    @Nullable
    private com.google.firebase.Timestamp toTimestamp(@Nullable Long dateUtcMs,
                                                      @Nullable Integer hour,
                                                      @Nullable Integer minute) {
        if (dateUtcMs == null || hour == null || minute == null) return null;

        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault());
        cal.setTimeInMillis(dateUtcMs);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        return new com.google.firebase.Timestamp(cal.getTime());
    }
    // endregion

}