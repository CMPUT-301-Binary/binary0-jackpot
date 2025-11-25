package com.example.jackpot;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Instrumented tests for UI flows related to the Organizer user role.
 * This class covers user stories such as creating events with various options like
 * QR codes, registration periods, and waiting list limits.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestEventsUIOrganizer {

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final List<String> testEventIds = new ArrayList<>();

    /**
     * Sets up the test environment before each test.
     * @throws Exception if setup fails.
     */
    @Before
    public void setUp() throws Exception {
        Intents.init();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    /**
     * Helper method to create and log in a new user with the ORGANIZER role.
     * @return The created User object.
     * @throws Exception if user creation or login fails.
     */
    private User createAndLoginOrganizer() throws Exception {
        String email = "organizer-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        User testUser = new User(uid, "Test Organizer", User.Role.ORGANIZER, email, "1234567890", "", "", "default", null, new GeoPoint(0.0, 0.0));
        Tasks.await(db.collection("users").document(uid).set(testUser), 10, TimeUnit.SECONDS);

        Thread.sleep(1000); // Allow time for login state to propagate
        return testUser;
    }

    /**
     * Tests the user story: "As an organizer, I want to upload an event poster to the event details page to provide visual information to entrants."
     * This test specifically covers UPDATING an existing poster.
     * @throws Exception if test execution fails.
     */
    @Test
    public void testUpdateEventPoster() throws Exception {
        User organizer = createAndLoginOrganizer();

        // 1. Setup: Create an event with an initial poster.
        String eventId = "update-poster-test-" + UUID.randomUUID().toString();
        String initialPosterUri = "initial_uri";
        Event testEvent = new Event(eventId, organizer.getId(), "Update Poster Test", "desc", new UserList(10), "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), initialPosterUri, "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));
        testEventIds.add(eventId);

        // 2. Navigate to the event's details page.
        onView(withId(R.id.nav_home)).perform(click()); // Refresh home
        Thread.sleep(3000);
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).perform(click());
        Thread.sleep(2000);

        // 3. Stub the photo picker to return a NEW image.
        Uri newImageUri = Uri.parse("android.resource://com.example.jackpot/" + R.drawable.avatar_2);
        Intent resultData = new Intent();
        resultData.setData(newImageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        // 4. Action: Click the update photo button.
        onView(withId(R.id.event_details_update_photo_button)).perform(click());

        // 5. Verification: Check that the poster URI in Firestore has changed.
        Thread.sleep(8000); // Wait for upload and DB write.

        Event updatedEvent = Tasks.await(db.collection("events").document(eventId).get()).toObject(Event.class);
        assertNotNull("Event could not be fetched from Firestore after update.", updatedEvent);
        assertNotNull("Poster URI is null after update.", updatedEvent.getPosterUri());
        assertNotEquals("Poster URI should have changed, but it matches the initial URI.", initialPosterUri, updatedEvent.getPosterUri());
    }
    
    /**
     * Tests the user story: "As an organizer, I want to upload an event poster..."
     * This test specifically covers creating a NEW event with a poster.
     * @throws Exception if test execution fails.
     */
    @Test
    public void testCreateEvent_WithPosterUpload() throws Exception {
        createAndLoginOrganizer();

        // 1. Navigate to Event Creation Fragment
        onView(withId(R.id.fab)).perform(click());
        Thread.sleep(1000);

        // 2. Stub the photo picker intent
        Uri dummyImageUri = Uri.parse("android.resource://com.example.jackpot/" + R.drawable.avatar_1);
        Intent resultData = new Intent();
        resultData.setData(dummyImageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        // 3. Fill out the form
        String eventName = "Poster Upload Test Event " + UUID.randomUUID().toString();
        onView(withId(R.id.editTextEventName)).perform(typeText(eventName), closeSoftKeyboard());
        onView(withId(R.id.editDescription)).perform(typeText("Event with a poster"), closeSoftKeyboard());
        onView(withId(R.id.editAddress)).perform(typeText("Online"), closeSoftKeyboard());
        onView(withId(R.id.editCapacity)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.editTextPrice)).perform(typeText("15.00"), closeSoftKeyboard());

        // Click the photo upload button (which will be intercepted)
        onView(withId(R.id.uploadImageButton)).perform(click());

        // 4. Submit the form
        onView(withId(R.id.buttonSubmit)).perform(click());

        // 5. Verification
        Thread.sleep(8000); // Wait for image upload and Firestore write

        QuerySnapshot snapshot = Tasks.await(db.collection("events").whereEqualTo("name", eventName).limit(1).get());
        assertFalse("No event with the test name was found in Firestore.", snapshot.isEmpty());

        Event createdEvent = snapshot.getDocuments().get(0).toObject(Event.class);
        assertNotNull("Created event could not be deserialized.", createdEvent);

        // Verify that a Poster Image URI was generated and saved
        assertNotNull("Poster URI should have been saved, but was null.", createdEvent.getPosterUri());
        assertFalse("Poster URI should not be empty.", createdEvent.getPosterUri().isEmpty());
    }

    /**
     * Tests the user story: "As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list."
     * This test covers the case where a limit is provided.
     * @throws Exception if test execution fails.
     */
    @Test
    public void testCreateEvent_WithWaitingListLimit() throws Exception {
        createAndLoginOrganizer();

        onView(withId(R.id.fab)).perform(click());
        Thread.sleep(1000);

        Uri dummyImageUri = Uri.parse("android.resource://com.example.jackpot/" + R.drawable.avatar_1);
        Intent resultData = new Intent();
        resultData.setData(dummyImageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        String eventName = "Waiting List Limit Test " + UUID.randomUUID().toString();
        onView(withId(R.id.editTextEventName)).perform(typeText(eventName), closeSoftKeyboard());
        onView(withId(R.id.editDescription)).perform(typeText("Event with a waiting list limit"), closeSoftKeyboard());
        onView(withId(R.id.editAddress)).perform(typeText("Online"), closeSoftKeyboard());
        onView(withId(R.id.editCapacity)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.editTextPrice)).perform(typeText("0"), closeSoftKeyboard());

        // Fill in the new waiting list limit field
        onView(withId(R.id.editWaitingListLimit)).perform(typeText("5"), closeSoftKeyboard());

        onView(withId(R.id.uploadImageButton)).perform(click());
        onView(withId(R.id.buttonSubmit)).perform(click());

        Thread.sleep(8000);

        QuerySnapshot snapshot = Tasks.await(db.collection("events").whereEqualTo("name", eventName).limit(1).get());
        assertFalse("No event with the test name was found in Firestore.", snapshot.isEmpty());

        Event createdEvent = snapshot.getDocuments().get(0).toObject(Event.class);
        assertNotNull("Created event could not be deserialized.", createdEvent);

        assertNotNull("Waiting list should not be null.", createdEvent.getWaitingList());
        assertNotNull("Waiting list capacity should have been set.", createdEvent.getWaitingList().getCapacity());
        assertEquals("Waiting list capacity does not match the set limit.", 5, (int) createdEvent.getWaitingList().getCapacity());
    }

    /**
     * Tests the user story: "As an organizer I want to OPTIONALLY limit the number of entrants..."
     * This test covers the case where the limit is left blank (is optional).
     * @throws Exception if test execution fails.
     */
    @Test
    public void testCreateEvent_WithoutWaitingListLimit() throws Exception {
        createAndLoginOrganizer();

        onView(withId(R.id.fab)).perform(click());
        Thread.sleep(1000);

        Uri dummyImageUri = Uri.parse("android.resource://com.example.jackpot/" + R.drawable.avatar_1);
        Intent resultData = new Intent();
        resultData.setData(dummyImageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        String eventName = "No Waiting List Limit Test " + UUID.randomUUID().toString();
        onView(withId(R.id.editTextEventName)).perform(typeText(eventName), closeSoftKeyboard());
        onView(withId(R.id.editDescription)).perform(typeText("Event with no waiting list limit"), closeSoftKeyboard());
        onView(withId(R.id.editAddress)).perform(typeText("Online"), closeSoftKeyboard());
        onView(withId(R.id.editCapacity)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.editTextPrice)).perform(typeText("0"), closeSoftKeyboard());

        // LEAVE WAITING LIST FIELD BLANK

        onView(withId(R.id.uploadImageButton)).perform(click());
        onView(withId(R.id.buttonSubmit)).perform(click());

        Thread.sleep(8000);

        QuerySnapshot snapshot = Tasks.await(db.collection("events").whereEqualTo("name", eventName).limit(1).get());
        assertFalse("No event with the test name was found in Firestore.", snapshot.isEmpty());

        Event createdEvent = snapshot.getDocuments().get(0).toObject(Event.class);
        assertNotNull("Created event could not be deserialized.", createdEvent);

        assertNotNull("Waiting list should not be null.", createdEvent.getWaitingList());
        assertNull("Waiting list capacity should be null when not set.", createdEvent.getWaitingList().getCapacity());
    }

    /**
     * Tests the user story: "As an organizer, I want to set a registration period."
     * Verifies that the start and end dates/times are correctly saved to Firestore.
     * @throws Exception if test execution fails.
     */
    @Test
    public void testCreateEvent_WithRegistrationPeriod() throws Exception {
        createAndLoginOrganizer();

        // 1. Navigate to Event Creation Fragment
        onView(withId(R.id.fab)).perform(click());
        Thread.sleep(1000);

        // 2. Stub the photo picker intent
        Uri dummyImageUri = Uri.parse("android.resource://com.example.jackpot/" + R.drawable.avatar_1);
        Intent resultData = new Intent();
        resultData.setData(dummyImageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        // 3. Fill out the form
        String eventName = "Registration Period Test Event " + UUID.randomUUID().toString();
        onView(withId(R.id.editTextEventName)).perform(typeText(eventName), closeSoftKeyboard());
        onView(withId(R.id.editDescription)).perform(typeText("Event with reg period"), closeSoftKeyboard());
        onView(withId(R.id.editAddress)).perform(typeText("Online"), closeSoftKeyboard());
        onView(withId(R.id.editCapacity)).perform(typeText("50"), closeSoftKeyboard());
        onView(withId(R.id.editTextPrice)).perform(typeText("10.00"), closeSoftKeyboard());

        onView(withId(R.id.spinnerCategory)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Charity"))).perform(click());

        // Set event date (must be after registration closes)
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_YEAR, 10);
        onView(withId(R.id.editDate)).perform(click());
        onView(withClassName(is(android.widget.DatePicker.class.getName()))).perform(PickerActions.setDate(eventDate.get(Calendar.YEAR), eventDate.get(Calendar.MONTH) + 1, eventDate.get(Calendar.DAY_OF_MONTH)));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.editTime)).perform(click());
        onView(withClassName(is(android.widget.TimePicker.class.getName()))).perform(PickerActions.setTime(19, 0));
        onView(withText("OK")).perform(click());

        // --- Set the Registration Period ---
        Calendar regOpenCal = Calendar.getInstance();
        regOpenCal.add(Calendar.DAY_OF_YEAR, 1);
        onView(withId(R.id.editRegOpenDate)).perform(click());
        onView(withClassName(is(android.widget.DatePicker.class.getName()))).perform(PickerActions.setDate(regOpenCal.get(Calendar.YEAR), regOpenCal.get(Calendar.MONTH) + 1, regOpenCal.get(Calendar.DAY_OF_MONTH)));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.editRegOpenTime)).perform(click());
        onView(withClassName(is(android.widget.TimePicker.class.getName()))).perform(PickerActions.setTime(9, 0)); // 9:00 AM
        onView(withText("OK")).perform(click());

        Calendar regCloseCal = Calendar.getInstance();
        regCloseCal.add(Calendar.DAY_OF_YEAR, 3);
        onView(withId(R.id.editRegCloseDate)).perform(click());
        onView(withClassName(is(android.widget.DatePicker.class.getName()))).perform(PickerActions.setDate(regCloseCal.get(Calendar.YEAR), regCloseCal.get(Calendar.MONTH) + 1, regCloseCal.get(Calendar.DAY_OF_MONTH)));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.editRegCloseTime)).perform(click());
        onView(withClassName(is(android.widget.TimePicker.class.getName()))).perform(PickerActions.setTime(17, 0)); // 5:00 PM
        onView(withText("OK")).perform(click());

        onView(withId(R.id.uploadImageButton)).perform(click());

        // 4. Submit the form
        onView(withId(R.id.buttonSubmit)).perform(click());

        // 5. Verification
        Thread.sleep(8000);

        QuerySnapshot snapshot = Tasks.await(db.collection("events").whereEqualTo("name", eventName).limit(1).get());
        assertFalse("No event with the test name was found in Firestore.", snapshot.isEmpty());

        Event createdEvent = snapshot.getDocuments().get(0).toObject(Event.class);
        assertNotNull("Created event could not be deserialized.", createdEvent);

        assertNotNull("Registration open date should not be null.", createdEvent.getRegOpenAt());
        assertNotNull("Registration close date should not be null.", createdEvent.getRegCloseAt());

        Calendar openResultCal = Calendar.getInstance();
        openResultCal.setTime(createdEvent.getRegOpenAt());
        assertEquals(regOpenCal.get(Calendar.YEAR), openResultCal.get(Calendar.YEAR));
        assertEquals(regOpenCal.get(Calendar.MONTH), openResultCal.get(Calendar.MONTH));
        assertEquals(regOpenCal.get(Calendar.DAY_OF_MONTH), openResultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, openResultCal.get(Calendar.HOUR_OF_DAY));

        Calendar closeResultCal = Calendar.getInstance();
        closeResultCal.setTime(createdEvent.getRegCloseAt());
        assertEquals(regCloseCal.get(Calendar.YEAR), closeResultCal.get(Calendar.YEAR));
        assertEquals(regCloseCal.get(Calendar.MONTH), closeResultCal.get(Calendar.MONTH));
        assertEquals(regCloseCal.get(Calendar.DAY_OF_MONTH), closeResultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, closeResultCal.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Tests the user story: "As an organizer I want to create a new event and generate a unique promotional QR code..."
     * @throws Exception if test execution fails.
     */
    @Test
    public void testCreateEvent_WithPromotionalQRCode() throws Exception {
        createAndLoginOrganizer();

        // 1. Navigate to Event Creation Fragment
        onView(withId(R.id.fab)).perform(click());
        Thread.sleep(1000);

        // 2. Stub the photo picker intent
        Uri dummyImageUri = Uri.parse("android.resource://com.example.jackpot/" + R.drawable.avatar_1);
        Intent resultData = new Intent();
        resultData.setData(dummyImageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        // 3. Fill out the form
        String eventName = "Test QR Code Event " + UUID.randomUUID().toString();
        onView(withId(R.id.editTextEventName)).perform(typeText(eventName), closeSoftKeyboard());
        onView(withId(R.id.editDescription)).perform(typeText("Event with a QR code"), closeSoftKeyboard());
        onView(withId(R.id.editAddress)).perform(typeText("Online"), closeSoftKeyboard());
        onView(withId(R.id.editCapacity)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.editTextPrice)).perform(typeText("5.00"), closeSoftKeyboard());

        // Select category
        onView(withId(R.id.spinnerCategory)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Party"))).perform(click());

        // Set a future date
        onView(withId(R.id.editDate)).perform(click());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 5);
        onView(withClassName(is(android.widget.DatePicker.class.getName()))).perform(PickerActions.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.editTime)).perform(click());
        onView(withClassName(is(android.widget.TimePicker.class.getName()))).perform(PickerActions.setTime(18, 30));
        onView(withText("OK")).perform(click());

        // Click the photo upload button (which will be intercepted)
        onView(withId(R.id.uploadImageButton)).perform(click());

        // Check the QR code box
        onView(withId(R.id.qrCodeBox)).perform(click());

        // 4. Submit the form
        onView(withId(R.id.buttonSubmit)).perform(click());

        // 5. Verification
        Thread.sleep(8000); // Wait for image upload and Firestore write

        QuerySnapshot snapshot = Tasks.await(db.collection("events").whereEqualTo("name", eventName).limit(1).get());
        assertFalse("No event with the test name was found in Firestore.", snapshot.isEmpty());

        Event createdEvent = snapshot.getDocuments().get(0).toObject(Event.class);
        assertNotNull("Created event could not be deserialized.", createdEvent);

        // Verify that a QR code ID and a Poster Image were generated and saved
        assertNotNull("QR Code ID should have been generated, but was null.", createdEvent.getQrCodeId());
        assertFalse("QR Code ID should not be empty.", createdEvent.getQrCodeId().isEmpty());
        
        assertNotNull("Poster URI should have been saved, but was null.", createdEvent.getPosterUri());
        assertFalse("Poster URI should not be empty.", createdEvent.getPosterUri().isEmpty());
    }

    /**
     * Cleans up the test environment by deleting any created users and events from Firebase.
     * @throws Exception if cleanup fails.
     */
    @After
    public void tearDown() throws Exception {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            try {
                // Clean up any events created during the test
                QuerySnapshot eventsToDelete = Tasks.await(db.collection("events").whereEqualTo("organizerId", currentUser.getUid()).get());
                for (int i = 0; i < eventsToDelete.size(); i++) {
                    db.collection("events").document(eventsToDelete.getDocuments().get(i).getId()).delete();
                }

                // Delete the user from Firestore and Auth
                db.collection("users").document(currentUser.getUid()).delete();
                Tasks.await(currentUser.delete());
            } catch (Exception e) {
                Log.w("TestEventsUIOrganizer", "Could not delete test data during teardown: " + e.getMessage());
            }
        }
        Intents.release();
    }
}
