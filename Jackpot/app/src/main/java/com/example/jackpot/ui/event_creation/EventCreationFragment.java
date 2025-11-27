package com.example.jackpot.ui.event_creation;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jackpot.UserList;
import com.example.jackpot.R;
import com.example.jackpot.ui.image.Image;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


/**
 * Fragment which pops up to allow an organizer to create an event. The fragment is a form.
 * The form has multiple fields which accept the input of various details, and uploading of an image.
 * The details entered are saved to the database upon the pressing of "Submit"
 * Modified to require poster upload and create separate Image documents in Firestore.
 */
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
    private Spinner spinnerCategory;

    // Registration picker state
    private Long regOpenDateUtcMs = null, regCloseDateUtcMs = null;
    private Integer regOpenHour = null, regOpenMinute = null;
    private Integer regCloseHour = null, regCloseMinute = null;

    // for date & time pickers
    private Long selectedDateUtcMs = null;
    private Integer selectedHour = null, selectedMinute = null;

    /**
     * Required empty public constructor.
     */
    public EventCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    /**
     * The main logic of the form. The fields are initialized and the photopicker is written.
     */
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

        submitButton.setOnClickListener(v -> createEvent());
        db = FirebaseFirestore.getInstance();

        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        List<String> categories = Arrays.asList(
                "Select a category…", // INDEX 0 is just a hint
                "Party", "Concert", "Charity", "Fair"
        );
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );

        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

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

        selectedPhotoTextView = view.findViewById(R.id.selectedPhotoText);

        // Registers a photo picker activity launcher in single-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        selectedPhotoTextView.setText("Photo selected: " + uri.getLastPathSegment());
                        selectedImageUri = uri;
                    }
                    else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        selectPhotoButton = view.findViewById(R.id.uploadImageButton);
        selectPhotoButton.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Creates an event and stores it in the database.
     */
    public void createEvent() {
        // region Validation

        // POSTER IS REQUIRED
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a poster image", Toast.LENGTH_SHORT).show();
            selectPhotoButton.requestFocus();
            return;
        }

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
        Double priceVal = parseDoubleOrNull(editTextEventPrice.getText().toString());
        if (priceVal == null) { editTextEventPrice.setError("Enter a number"); editTextEventPrice.requestFocus(); return; }
        if (priceVal < 0)      { editTextEventPrice.setError("Must be ≥ 0");   editTextEventPrice.requestFocus(); return; }

        Integer capacityVal = parseIntOrNull(editTextEventCapacity.getText().toString());
        if (capacityVal == null) { editTextEventCapacity.setError("Enter an integer"); editTextEventCapacity.requestFocus(); return; }
        if (capacityVal < 1)     { editTextEventCapacity.setError("Must be ≥ 1");       editTextEventCapacity.requestFocus(); return; }

        final Integer[] waitLimitVal = {null};
        String waitStr = editWaitingListLimit.getText().toString().trim();
        if (!waitStr.isEmpty()) {
            waitLimitVal[0] = parseIntOrNull(waitStr);
            if (waitLimitVal[0] == null) { editWaitingListLimit.setError("Enter an integer"); editWaitingListLimit.requestFocus(); return; }
            if (waitLimitVal[0] < 0)     { editWaitingListLimit.setError("Must be ≥ 0");      editWaitingListLimit.requestFocus(); return; }
        } else {
            editWaitingListLimit.setError(null);
        }
        // endregion

        // region Build canonical timestamps from pickers
        com.google.firebase.Timestamp eventTs =
                toTimestamp(selectedDateUtcMs, selectedHour, selectedMinute);
        com.google.firebase.Timestamp regOpenTs =
                toTimestamp(regOpenDateUtcMs, regOpenHour, regOpenMinute);
        com.google.firebase.Timestamp regCloseTs =
                toTimestamp(regCloseDateUtcMs, regCloseHour, regCloseMinute);

        int catPos = spinnerCategory.getSelectedItemPosition();

        // region Validation for category and also canonical timestamps
        if (catPos <= 0) {
            View selectedView = spinnerCategory.getSelectedView();
            if (selectedView instanceof TextView) {
                ((TextView) selectedView).setError("Pick a category");
            }
            spinnerCategory.requestFocus();
            Toast.makeText(requireContext(), "Please choose a category", Toast.LENGTH_SHORT).show();
            return;
        }
        String category = (String) spinnerCategory.getSelectedItem();

        if (eventTs == null) {
            editTextEventDate.setError("Pick event date");
            editTextEventTime.setError("Pick event time");
            editTextEventDate.requestFocus();
            return;
        } else {
            editTextEventDate.setError(null);
            editTextEventTime.setError(null);
        }

        if (eventTs.compareTo(com.google.firebase.Timestamp.now()) <= 0) {
            editTextEventDate.setError("Event must be in the future");
            editTextEventTime.setError("Event must be in the future");
            editTextEventDate.requestFocus();
            return;
        }

        if (regOpenTs == null) {
            editRegOpenDate.setError("Pick open date");
            editRegOpenTime.setError("Pick open time");
            editRegOpenDate.requestFocus();
            return;
        } else {
            editRegOpenDate.setError(null);
            editRegOpenTime.setError(null);
        }

        if (regCloseTs == null) {
            editRegCloseDate.setError("Pick close date");
            editRegCloseTime.setError("Pick close time");
            editRegCloseDate.requestFocus();
            return;
        } else {
            editRegCloseDate.setError(null);
            editRegCloseTime.setError(null);
        }

        if (regOpenTs.compareTo(regCloseTs) >= 0) {
            editRegCloseDate.setError("Close must be after open");
            editRegCloseTime.setError("Close must be after open");
            editRegCloseDate.requestFocus();
            return;
        }

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
        boolean geoLocation = geoLocationBox.isChecked();
        boolean qrCode = qrCodeBox.isChecked();
        // endregion

        // Current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : null;

        String eventId = UUID.randomUUID().toString();

        // region Build the event payload
        Map<String, Object> eventDoc = new HashMap<>();
        eventDoc.put("eventId", eventId);
        eventDoc.put("name", eventName);
        eventDoc.put("description", eventDescription);
        eventDoc.put("location", eventLocation);

        eventDoc.put("date", eventDate);
        eventDoc.put("time", eventTime);

        eventDoc.put("eventAt", eventTs);
        eventDoc.put("regOpenAt", regOpenTs);
        eventDoc.put("regCloseAt", regCloseTs);

        eventDoc.put("price", priceVal);
        eventDoc.put("capacity", capacityVal);
        if (waitLimitVal[0] == null) {
            waitLimitVal[0] = 0;
        }
        eventDoc.put("waitingListLimit", waitLimitVal[0]);

        eventDoc.put("geoLocation", geoLocation);
        eventDoc.put("qrCode", qrCode);
        eventDoc.put("createdBy", userId);
        eventDoc.put("createdAt", FieldValue.serverTimestamp());

        eventDoc.put("regOpenDate", editRegOpenDate.getText().toString().trim());
        eventDoc.put("regOpenTime", editRegOpenTime.getText().toString().trim());
        eventDoc.put("regCloseDate", editRegCloseDate.getText().toString().trim());
        eventDoc.put("regCloseTime", editRegCloseTime.getText().toString().trim());
        eventDoc.put("category", category);

        UserList waitingList = new UserList(waitLimitVal[0]);
        eventDoc.put("waitingList", waitingList);
        UserList joinedList = new UserList(capacityVal);
        eventDoc.put("joinedList", joinedList);
        UserList invitedList = new UserList(0);
        eventDoc.put("invitedList", invitedList);
        // endregion

        submitButton.setEnabled(false);

        // Upload poster (now required) and create event
        uploadPosterAndCreateEvent(eventId, eventDoc, userId, qrCode);
    }

    /**
     * Uploads the poster image and creates separate Image documents in Firestore.
     */
    private void uploadPosterAndCreateEvent(String eventId, Map<String, Object> eventDoc, String userId, boolean generateQR) {
        String imageName = "posters/" + UUID.randomUUID().toString() + ".png";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imageName);

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String posterUrl = downloadUri.toString();
                eventDoc.put("posterUri", posterUrl);

                // Create Image document for poster
                String posterImageId = UUID.randomUUID().toString();
                Image posterImage = new Image(
                        posterImageId,
                        userId,
                        posterUrl,
                        Image.TYPE_POSTER,
                        Image.ORDER_POSTER,
                        eventId
                );

                // Save poster image to images collection
                savePosterImageDocument(eventId, posterImage, eventDoc, userId, generateQR);
            });
        }).addOnFailureListener(e -> {
            submitButton.setEnabled(true);
            Toast.makeText(requireContext(), "Failed to upload poster: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Saves the poster Image document to Firestore.
     */
    private void savePosterImageDocument(String eventId, Image posterImage, Map<String, Object> eventDoc, String userId, boolean generateQR) {
        Map<String, Object> posterDoc = new HashMap<>();
        posterDoc.put("imageID", posterImage.getImageID());
        posterDoc.put("uploadedBy", posterImage.getUploadedBy());
        posterDoc.put("imageUrl", posterImage.getImageUrl());
        posterDoc.put("imageType", posterImage.getImageType());
        posterDoc.put("displayOrder", posterImage.getDisplayOrder());
        posterDoc.put("createdAt", FieldValue.serverTimestamp());
        posterDoc.put("eventId", posterImage.getEventId());

        db.collection("images")
                .document(posterImage.getImageID())
                .set(posterDoc)
                .addOnSuccessListener(v -> {
                    Log.d("ImageSave", "Poster image document created: " + posterImage.getImageID());

                    // Now save the event
                    if (generateQR) {
                        generateQRAndSaveEvent(eventId, eventDoc, userId);
                    } else {
                        saveEventToFirestore(eventId, eventDoc);
                    }
                })
                .addOnFailureListener(e -> {
                    submitButton.setEnabled(true);
                    Log.e("ImageSave", "Failed to save poster image document", e);
                    Toast.makeText(requireContext(), "Failed to save poster image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Generates a QR code and saves it as a separate Image document.
     */
    private void generateQRAndSaveEvent(String eventId, Map<String, Object> eventDoc, String userId) {
        String qrContent = "jackpot://event/" + eventId;

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    600,
                    600
            );
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap qrBitmap = encoder.createBitmap(matrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] qrData = baos.toByteArray();

            String qrName = "qrcodes/" + eventId + ".png";
            StorageReference qrRef = FirebaseStorage.getInstance().getReference().child(qrName);

            qrRef.putBytes(qrData)
                    .addOnSuccessListener(taskSnapshot -> qrRef.getDownloadUrl().addOnSuccessListener(qrUri -> {
                        String qrUrl = qrUri.toString();
                        Log.d("QRUpload", "QR code uploaded: " + qrUrl);

                        // Store QR reference in main event document
                        eventDoc.put("qrCodeImage", qrUrl);

                        // Create Image document for QR code
                        String qrImageId = UUID.randomUUID().toString();
                        Image qrImage = new Image(
                                qrImageId,
                                userId,
                                qrUrl,
                                Image.TYPE_QR_CODE,
                                Image.ORDER_QR_CODE,
                                eventId
                        );

                        // Save QR image document
                        eventDoc.put("qrCodeId", qrImageId);
                        saveQRImageDocument(eventId, qrImage, eventDoc);
                    }))
                    .addOnFailureListener(e -> {
                        submitButton.setEnabled(true);
                        Log.e("QRUpload", "Failed to upload QR code", e);
                        Toast.makeText(requireContext(), "Failed to create QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (WriterException e) {
            submitButton.setEnabled(true);
            Log.e("QRGen", "Failed to generate QR code", e);
            Toast.makeText(requireContext(), "Failed to generate QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves the QR code Image document to Firestore.
     */
    private void saveQRImageDocument(String eventId, Image qrImage, Map<String, Object> eventDoc) {
        Map<String, Object> qrDoc = new HashMap<>();
        qrDoc.put("imageID", qrImage.getImageID());
        qrDoc.put("uploadedBy", qrImage.getUploadedBy());
        qrDoc.put("imageUrl", qrImage.getImageUrl());
        qrDoc.put("imageType", qrImage.getImageType());
        qrDoc.put("displayOrder", qrImage.getDisplayOrder());
        qrDoc.put("createdAt", FieldValue.serverTimestamp());
        qrDoc.put("eventId", qrImage.getEventId());


        db.collection("images")
                .document(qrImage.getImageID())
                .set(qrDoc)
                .addOnSuccessListener(v -> {
                    Log.d("ImageSave", "QR code image document created: " + qrImage.getImageID());

                    // Finally save the event
                    saveEventToFirestore(eventId, eventDoc);
                })
                .addOnFailureListener(e -> {
                    submitButton.setEnabled(true);
                    Log.e("ImageSave", "Failed to save QR image document", e);
                    Toast.makeText(requireContext(), "Failed to save QR image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Saves the event to Firestore.
     */
    private void saveEventToFirestore(String eventId, Map<String, Object> eventDoc) {
        db.collection("events")
                .document(eventId)
                .set(eventDoc)
                .addOnSuccessListener(v -> {
                    Log.d("EventCreation", "Event created: " + eventId);
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    submitButton.setEnabled(true);
                    Log.e("Firestore", "Failed to create event", e);
                    Toast.makeText(requireContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Opens the date picker.
     */
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

    /**
     * Opens the time picker.
     */
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

    /**
     * Opens the date picker for registration.
     */
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

    /**
     * Opens the time picker for registration.
     */
    private void openRegTimePicker(boolean isOpen) {
        int defHour = isOpen ? (regOpenHour  == null ? 9  : regOpenHour)  : (regCloseHour  == null ? 17 : regCloseHour);
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

    /**
     * Checks if a field has text.
     */
    private boolean requireText(EditText et, String msg) {
        if (et.getText().toString().trim().isEmpty()) {
            et.setError(msg);
            et.requestFocus();
            return false;
        }
        et.setError(null);
        return true;
    }

    /**
     * Parses a string to an integer.
     */
    @Nullable
    private Integer parseIntOrNull(String s) {
        try { return s.trim().isEmpty() ? null : Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Parses a string to a double.
     */
    @Nullable
    private Double parseDoubleOrNull(String s) {
        try { return s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Converts date and time picker values to a Firestore Timestamp.
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
}