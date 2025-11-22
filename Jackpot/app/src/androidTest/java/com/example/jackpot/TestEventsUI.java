package com.example.jackpot;

import android.content.Context;
import android.util.Log;
import android.widget.DatePicker;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.jackpot.activities.ui.SignupEntrantActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matchers;
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

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestEventsUI {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User testUser;
    private final List<String> testEventIds = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Intents.init();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        // Sign out to ensure tests start from a clean state
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    private void createAndLoginTestUser() throws Exception {
        String email = "testuser-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        this.testUser = new User(uid, "Test Entrant", User.Role.ENTRANT, email, "1234567890", "","", "default", null, new GeoPoint(0.0,0.0));
        Tasks.await(db.collection("users").document(uid).set(this.testUser), 10, TimeUnit.SECONDS);
        Thread.sleep(1000); // Allow time for login state to propagate
    }

    @Test
    public void testEntrantSignup_AndDataIsSaved() throws Exception {
        // 1. Launch the SignupEntrantActivity directly
        ActivityScenario<SignupEntrantActivity> scenario = ActivityScenario.launch(SignupEntrantActivity.class);

        // 2. Define unique user data
        String name = "Jane Doe";
        String phone = "9876543210";
        String email = "jane.doe." + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        // 3. UI Interaction: Fill in the form and click sign up
        onView(withId(R.id.namefld)).perform(typeText(name), closeSoftKeyboard());
        onView(withId(R.id.emailfld)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.passwordfld)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.phonefld)).perform(typeText(phone), closeSoftKeyboard());
        onView(withId(R.id.signupbtn)).perform(click());

        // 4. Wait for async operations (Auth and Firestore) to complete
        Thread.sleep(5000);

        // 5. Verification: Check if user exists in Auth and data is correct in Firestore
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        assertNotNull("User was not created in Firebase Authentication.", firebaseUser);
        String uid = firebaseUser.getUid();

        DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(uid).get());
        assertTrue("User document was not created in Firestore.", userDoc.exists());

        User savedUser = userDoc.toObject(User.class);
        assertNotNull("User object could not be deserialized from Firestore.", savedUser);

        // Assert that the saved data matches the input data
        assertEquals("Name does not match input.", name, savedUser.getName());
        assertEquals("Email does not match input.", email, savedUser.getEmail());
        assertEquals("Phone does not match input.", phone, savedUser.getPhone());
    }

    @Test
    public void testSeesListOfEventsOnHomeFragment() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create two new events in the database.
        String eventId1 = "test-event-" + UUID.randomUUID().toString();
        Event testEvent1 = new Event(eventId1, "org", "Test Event 1", "desc", new UserList(10), "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId1).set(testEvent1));
        testEventIds.add(eventId1);

        String eventId2 = "test-event-" + UUID.randomUUID().toString();
        Event testEvent2 = new Event(eventId2, "org", "Test Event 2", "desc", new UserList(10), "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId2).set(testEvent2));
        testEventIds.add(eventId2);

        // 2. Refresh the UI to ensure it fetches the new data.
        onView(withId(R.id.nav_events)).perform(click()); // Navigate away
        Thread.sleep(1000);
        onView(withId(R.id.nav_home)).perform(click()); // Navigate back home
        Thread.sleep(3000); // Wait for events to load

        // 3. Verification: Check that the list view now displays the two events we created.
        onView(withId(R.id.events_list)).check(matches(hasChildCount(2)));
    }

    @Test
    public void testEntrantJoinsEventFromHome() throws Exception {
        createAndLoginTestUser();
        // Setup: Create a single event to join.
        String eventId = "join-test-event-" + UUID.randomUUID().toString();
        testEventIds.add(eventId);
        Event testEvent = new Event(eventId, "org", "Joinable Event", "desc", new UserList(10), "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));

        // Refresh UI
        onView(withId(R.id.nav_events)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.join_button))
                .perform(click());

        Thread.sleep(2000);

        DocumentSnapshot updatedEventDoc = Tasks.await(db.collection("events").document(eventId).get());
        assertTrue("Event document could not be found after joining.", updatedEventDoc.exists());
        Event updatedEvent = updatedEventDoc.toObject(Event.class);
        assertNotNull("Event object could not be deserialized.", updatedEvent);
        assertNotNull("Waiting list is null.", updatedEvent.getWaitingList());

        boolean isUserInWaitingList = updatedEvent.getWaitingList().getUsers().stream()
                .anyMatch(entrant -> entrant.getId().equals(testUser.getId()));

        assertTrue("Test user was not found in the waiting list after clicking join.", isUserInWaitingList);

        intended(not(hasComponent(EventDetailsActivity.class.getName())));
    }

    @Test
    public void testEntrantLeavesEventFromEventsFragment() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create an event and add the test user to its waiting list.
        String eventId = "leave-test-event-" + UUID.randomUUID().toString();
        testEventIds.add(eventId); // Add for cleanup
        Event testEvent = new Event(eventId, "organizer-id", "Test Event for Leaving",
                "Desc", new UserList(), "Location", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), null, "qr", false, "");

        Entrant entrantUser = new Entrant(testUser.getId(), testUser.getName(), testUser.getRole(), testUser.getEmail(), testUser.getPhone(), testUser.getProfileImageUrl(),testUser.getPassword(), testUser.getNotificationPreferences(), testUser.getDevice(), testUser.getGeoPoint());
        testEvent.addEntrantWaitingList(entrantUser);

        Tasks.await(db.collection("events").document(eventId).set(testEvent));

        // 2. Navigate to the Events Fragment.
        onView(withId(R.id.nav_events_entrant)).perform(click());
        Thread.sleep(3000); // Wait for fragment to load and fetch data.

        // 3. Find the event and click the "Leave Lottery" button.
        onData(anything())
                .inAdapterView(withId(R.id.entrant_events))
                .atPosition(0)
                .onChildView(withId(R.id.leave_button))
                .perform(click());

        Thread.sleep(2000); // Wait for Firestore update.

        // 4. Verification: Fetch the event and verify the user is no longer in the waiting list.
        DocumentSnapshot updatedEventDoc = Tasks.await(db.collection("events").document(eventId).get());
        Event updatedEvent = updatedEventDoc.toObject(Event.class);
        assertNotNull(updatedEvent);

        boolean isUserInWaitingList = updatedEvent.getWaitingList().getUsers().stream()
                .anyMatch(entrant -> entrant.getId().equals(testUser.getId()));

        assertFalse("Test user should NOT be in the waiting list after leaving.", isUserInWaitingList);
    }

    @Test
    public void testFilterEventsByCategory() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create events with distinct categories to test filtering.
        String partyEventId = "filter-test-party-" + UUID.randomUUID().toString();
        Event partyEvent = new Event(partyEventId, "org", "Party Night", "desc", new UserList(10), "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "Party");
        Tasks.await(db.collection("events").document(partyEventId).set(partyEvent));
        testEventIds.add(partyEventId);

        String concertEventId = "filter-test-concert-" + UUID.randomUUID().toString();
        Event concertEvent = new Event(concertEventId, "org", "Rock Concert", "desc", new UserList(10), "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "Concert");
        Tasks.await(db.collection("events").document(concertEventId).set(concertEvent));
        testEventIds.add(concertEventId);

        // 2. Refresh the UI to load the new events.
        onView(withId(R.id.nav_events)).perform(click()); // Navigate away
        Thread.sleep(1000);
        onView(withId(R.id.nav_home)).perform(click()); // Navigate back home
        Thread.sleep(3000); // Wait for events to load.

        // 3. Action: Click the "Party" filter button.
        onView(withId(R.id.party_button)).perform(click());
        Thread.sleep(1000); // Wait for UI to filter.

        // 4. Verification: Check that only one event is shown and it's the correct one.
        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));

        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.event_name))
                .check(matches(withText("Party Night")));

        // 5. Action: Clear filter and verify both events are back.
        onView(withId(R.id.clear_filters_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.events_list)).check(matches(hasChildCount(2)));
    }

    @Test
    public void testFilterEventsByLocation() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create events with distinct locations.
        String edmontonEventId = "loc-test-edm-" + UUID.randomUUID().toString();
        Event edmontonEvent = new Event(edmontonEventId, "org", "Edmonton Expo", "desc", new UserList(10), "Edmonton", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(edmontonEventId).set(edmontonEvent));
        testEventIds.add(edmontonEventId);

        String calgaryEventId = "loc-test-cal-" + UUID.randomUUID().toString();
        Event calgaryEvent = new Event(calgaryEventId, "org", "Calgary Stampede", "desc", new UserList(10), "Calgary", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(calgaryEventId).set(calgaryEvent));
        testEventIds.add(calgaryEventId);

        // 2. Refresh UI.
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        // 3. Action: Click location button and enter text.
        onView(withId(R.id.locationButton)).perform(click());
        onView(withClassName(Matchers.equalTo("android.widget.EditText"))).perform(typeText("Edmonton"), closeSoftKeyboard());
        onView(withText("Confirm")).perform(click());
        Thread.sleep(1000);

        // 4. Verification: Check that only the Edmonton event is shown.
        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.event_name)).check(matches(withText("Edmonton Expo")));
    }

    @Test
    public void testFilterEventsByDate() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create events with distinct dates.
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        String todayEventId = "date-test-today-" + UUID.randomUUID().toString();
        Event todayEvent = new Event(todayEventId, "org", "Today's Market", "desc", new UserList(10), "loc", today.getTime(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(todayEventId).set(todayEvent));
        testEventIds.add(todayEventId);

        String tomorrowEventId = "date-test-tmrw-" + UUID.randomUUID().toString();
        Event tomorrowEvent = new Event(tomorrowEventId, "org", "Tomorrow's Gala", "desc", new UserList(10), "loc", tomorrow.getTime(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(tomorrowEventId).set(tomorrowEvent));
        testEventIds.add(tomorrowEventId);

        // 2. Refresh UI.
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        // 3. Action: Click date button and select tomorrow.
        onView(withId(R.id.timeButton)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH) + 1, tomorrow.get(Calendar.DAY_OF_MONTH)));
        onView(withText("OK")).perform(click());
        Thread.sleep(1000);

        // 4. Verification: Check that only the event for tomorrow is shown.
        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.event_name)).check(matches(withText("Tomorrow's Gala")));
    }

    @Test
    public void testUpdateProfile_AndDataIsSaved() throws Exception {
        // 1. Ensure a user is logged in
        createAndLoginTestUser();

        // 2. Navigate to the Profile Fragment
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(2000); // Wait for profile to load

        // 3. Define new data to update
        String newName = "John Smith";
        String newPhone = "5551234567";

        // 4. UI Interaction: Clear existing text, type new text, and save
        onView(withId(R.id.profile_name)).perform(clearText(), typeText(newName), closeSoftKeyboard());
        onView(withId(R.id.profile_phone)).perform(clearText(), typeText(newPhone), closeSoftKeyboard());
        onView(withId(R.id.save_profile_button)).perform(click());

        // 5. Wait for async operations (Firestore update) to complete
        Thread.sleep(3000); // Give time for the toast and DB update

        // 6. Verification: Fetch the user document from Firestore and verify the fields were updated
        DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(testUser.getId()).get());
        assertTrue("User document does not exist in Firestore.", userDoc.exists());

        // Assert that the updated data matches the input
        assertEquals("Name was not updated in Firestore.", newName, userDoc.getString("name"));
        assertEquals("Phone was not updated in Firestore.", newPhone, userDoc.getString("phone"));
    }

    @After
    public void tearDown() throws Exception {
        // Delete the user from Firebase to keep the database clean for the next test run.
        if (mAuth.getCurrentUser() != null) {
            try {
                Tasks.await(mAuth.getCurrentUser().delete());
            } catch (Exception e) {
                // This can happen if the user was already deleted or auth state is unstable
                Log.w("TestCleanup", "Could not delete test user: " + e.getMessage());
            }
        }

        // Cleanup all events created during tests
        for (String eventId : testEventIds) {
            db.collection("events").document(eventId).delete();
        }
        testEventIds.clear();

        Intents.release();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.jackpot", appContext.getPackageName());
    } 
}
