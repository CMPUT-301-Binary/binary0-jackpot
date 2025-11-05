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

    private Uri selectedImageUri;


    public EventCreationFragment(){
        ;
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
    public void createEvent(){
        //Create the event object

        //Get the data from the form
        String eventName = editTextEventName.getText().toString();
        String eventDescription = editTextEventDescription.getText().toString();
        String eventLocation = editTextEventLocation.getText().toString();
        String eventDate = editTextEventDate.getText().toString();
        String eventTime = editTextEventTime.getText().toString();
        String eventCapacity = editTextEventCapacity.getText().toString();
        String eventPrice = editTextEventPrice.getText().toString();
        boolean geoLocation = geoLocationBox.isChecked();
        boolean qrCode = qrCodeBox.isChecked();
        String userId;

        //Generate a UUID for the event
        String eventId = UUID.randomUUID().toString();
        //Get the current id of the user who is logged in.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
             userId = user.getUid();
        }
        else{
             userId = null;
        }

        //Create an Image object (the poster)











    }



}