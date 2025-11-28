package com.example.jackpot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.DatePicker;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
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
import static org.hamcrest.CoreMatchers.containsString;
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
 * Instrumented tests for UI flows related to the Entrant user role.
 * This class covers user stories such as signing up, browsing events, joining waiting lists,
 * and viewing event details via deep links.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestEventsUIEntrant {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User testUser;
    private final List<String> testEventIds = new ArrayList<>();
    private final List<String> testUserIds = new ArrayList<>();


    /**
     * Sets up the test environment. Initializes Firebase, disables app verification for testing,
     * and ensures no user is signed in before each test runs.
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
     * Helper method to create and log in a new user with the ENTRANT role.
     * @throws Exception if user creation or Firestore write fails.
     */
    private void createAndLoginTestUser() throws Exception {
        String email = "testuser-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        this.testUser = new User(uid, "Test Entrant", User.Role.ENTRANT, email, "1234567890", "","", "default", null, new GeoPoint(0.0,0.0));
        Tasks.await(db.collection("users").document(uid).set(this.testUser), 10, TimeUnit.SECONDS);
        testUserIds.add(uid);
        Thread.sleep(1000); 
    }

    /**
     * Tests the user story: "As an entrant, I want to view event details within the app by scanning the promotional QR code."
     * This is simulated by launching the app with a deep link intent.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testViewEventDetails_ViaQRCodeDeepLink() throws Exception {
        // 1. Setup: Create a user and a target event.
        createAndLoginTestUser();
        String eventId = "deep-link-event-" + UUID.randomUUID().toString();
        String eventName = "Deep Link Gala";
        Event testEvent = new Event(eventId, "org", eventName, "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));
        testEventIds.add(eventId);

        // 2. Action: Simulate a deep link scan by creating and launching an intent.
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("jackpot://event/" + eventId));
        intent.setPackage(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        ActivityScenario.launch(intent);

        // 3. Wait for the activity to process the deep link and load data.
        Thread.sleep(4000);

        // 4. Verification: Check that the EventDetailsActivity is showing the correct event name.
        onView(withId(R.id.event_details_name)).check(matches(withText(eventName)));
    }

    /**
     * Tests the user story: "As an entrant, I want to be able to sign up for an event from the event details."
     * @throws Exception if test setup fails.
     */
    @Test
    public void testEntrantJoinsEventFromDetailsActivity() throws Exception {
        // 1. Setup: Create a user and a new event to join.
        createAndLoginTestUser();
        String eventId = "details-join-test-" + UUID.randomUUID().toString();
        Event testEvent = new Event(eventId, "org", "Details Join Test", "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));
        testEventIds.add(eventId);

        // 2. Refresh UI to load the new event.
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000); // Wait for HomeFragment to reload.

        // 3. Action: Click on the event to open details.
        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0) // Assuming it's the first event
                .perform(click());
        
        // Verify we are in the details activity
        intended(hasComponent(EventDetailsActivity.class.getName()));
        Thread.sleep(1000); // Wait for details activity to load

        // 4. Action: Click the join button within the details activity.
        onView(withId(R.id.event_details_join_button)).perform(click());

        // 5. Wait for the asynchronous Firestore update.
        Thread.sleep(2000);

        // 6. Verification: Fetch the event from Firestore and check if the user is in the waiting list.
        DocumentSnapshot updatedEventDoc = Tasks.await(db.collection("events").document(eventId).get());
        assertTrue("Event document could not be found after joining.", updatedEventDoc.exists());
        Event updatedEvent = updatedEventDoc.toObject(Event.class);
        assertNotNull("Event object could not be deserialized.", updatedEvent);
        assertNotNull("Waiting list is null.", updatedEvent.getWaitingList());

        boolean isUserInWaitingList = updatedEvent.getWaitingList().getUsers().stream()
                .anyMatch(entrant -> entrant.getId().equals(testUser.getId()));

        assertTrue("Test user was not found in the waiting list after joining from details.", isUserInWaitingList);
    }

    /**
     * Tests the user story: "As an entrant, I want to be informed about the criteria or guidelines for the lottery selection process."
     * @throws Exception if test setup fails.
     */
    @Test
    public void testLotteryGuidelinesDisplayed() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create an event with specific lottery guidelines in its description.
        String eventId = "guidelines-test-" + UUID.randomUUID().toString();
        String guidelines = "Lottery winners are selected at random from the waiting list.";
        Event testEvent = new Event(eventId, "org", "Lottery Info Event", guidelines, new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));
        testEventIds.add(eventId);

        // 2. Refresh UI to load the new event.
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        // 3. Action: Click on the event to open its details.
        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .perform(click());

        // 4. Verification:
        // Check that the EventDetailsActivity was launched.
        intended(hasComponent(EventDetailsActivity.class.getName()));
        // Check that the description on the details screen contains the guidelines.
        onView(withId(R.id.event_details_description)).check(matches(withText(containsString(guidelines))));
    }

    /**
     * Tests the user story: "As an entrant, I want to know how many total entrants are on the waiting list for an event."
     * Verifies the count on both the home list and the events screen, and that it updates live.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testWaitingListCountIsDisplayedCorrectly() throws Exception {
        createAndLoginTestUser();
        // 1. Setup: Create an event and add 3 dummy users to its waiting list.
        String eventId = "waiting-list-test-" + UUID.randomUUID().toString();
        Event testEvent = new Event(eventId, "org", "Waiting List Test Event", "desc", new UserList(20), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 20, new Date(), new Date(), "", "", false, "cat");
        
        for (int i = 0; i < 3; i++) {
            User dummyUser = new User("dummy-id-" + i, "Dummy " + i, User.Role.ENTRANT, "", "", "", "", "", null, null);
            testEvent.getWaitingList().add(dummyUser);
        }
        Tasks.await(db.collection("events").document(eventId).set(testEvent));
        testEventIds.add(eventId);

        // 2. Refresh UI and navigate to Home.
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        // 3. Verification on HomeFragment: Check initial count is 3.
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.event_waiting)).check(matches(withText("3 waiting")));

        // 4. Action: Main user joins the event.
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.join_button)).perform(click());
        Thread.sleep(1000);

        // 5. Verification on HomeFragment: Check count updates to 4.
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.event_waiting)).check(matches(withText("4 waiting")));

        // 6. Verification on EventsFragment: Navigate and check count is 4.
        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(2000);
        onData(anything()).inAdapterView(withId(R.id.entrant_events)).atPosition(0).onChildView(withId(R.id.eventPrice)).check(matches(withText(containsString("Waiting: 4"))));
    }

    /**
     * Cleans up the test environment by deleting any created users and events from Firebase.
     * @throws Exception if cleanup fails.
     */
    @After
    public void tearDown() throws Exception {
        if (mAuth.getCurrentUser() != null) {
            try {
                Tasks.await(mAuth.getCurrentUser().delete());
            } catch (Exception e) {
                Log.w("TestCleanup", "Could not delete test user: " + e.getMessage());
            }
        }

        for (String userId : testUserIds) {
            db.collection("users").document(userId).delete();
        }
        testUserIds.clear();

        for (String eventId : testEventIds) {
            db.collection("events").document(eventId).delete();
        }
        testEventIds.clear();

        Intents.release();
    }


    /**
     * Tests the user story: "As an entrant, I want to provide my personal information such as name, email and optional phone number in the app."
     * @throws Exception if test setup fails.
     */
    @Test
    public void testEntrantSignup_AndDataIsSaved() throws Exception {
        ActivityScenario<SignupEntrantActivity> scenario = ActivityScenario.launch(SignupEntrantActivity.class);

        String name = "Jane Doe";
        String phone = "9876543210";
        String email = "jane.doe." + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        onView(withId(R.id.namefld)).perform(typeText(name), closeSoftKeyboard());
        onView(withId(R.id.emailfld)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.passwordfld)).perform(typeText(password), closeSoftKeyboard());
        onView(withId(R.id.phonefld)).perform(typeText(phone), closeSoftKeyboard());
        onView(withId(R.id.signupbtn)).perform(click());

        Thread.sleep(5000);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        assertNotNull("User was not created in Firebase Authentication.", firebaseUser);
        String uid = firebaseUser.getUid();
        testUserIds.add(uid);

        DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(uid).get());
        assertTrue("User document was not created in Firestore.", userDoc.exists());

        User savedUser = userDoc.toObject(User.class);
        assertNotNull("User object could not be deserialized from Firestore.", savedUser);

        assertEquals("Name does not match input.", name, savedUser.getName());
        assertEquals("Email does not match input.", email, savedUser.getEmail());
        assertEquals("Phone does not match input.", phone, savedUser.getPhone());
    }

    /**
     * Tests the user story: "As an entrant, I want to be able to see a list of events that I can join the waiting list for."
     * @throws Exception if test setup fails.
     */
    @Test
    public void testSeesListOfEventsOnHomeFragment() throws Exception {
        createAndLoginTestUser();
        String eventId1 = "test-event-" + UUID.randomUUID().toString();
        Event testEvent1 = new Event(eventId1, "org", "Test Event 1", "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId1).set(testEvent1));
        testEventIds.add(eventId1);

        String eventId2 = "test-event-" + UUID.randomUUID().toString();
        Event testEvent2 = new Event(eventId2, "org", "Test Event 2", "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId2).set(testEvent2));
        testEventIds.add(eventId2);

        onView(withId(R.id.nav_events)).perform(click()); 
        Thread.sleep(1000);
        onView(withId(R.id.nav_home)).perform(click()); 
        Thread.sleep(3000); 

        onView(withId(R.id.events_list)).check(matches(hasChildCount(2)));
    }

    /**
     * Tests joining an event from the home screen list.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testEntrantJoinsEventFromHome() throws Exception {
        createAndLoginTestUser();
        String eventId = "join-test-event-" + UUID.randomUUID().toString();
        testEventIds.add(eventId);
        Event testEvent = new Event(eventId, "org", "Joinable Event", "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));

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

    /**
     * Tests leaving an event from the "My Events" screen.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testEntrantLeavesEventFromEventsFragment() throws Exception {
        createAndLoginTestUser();
        String eventId = "leave-test-event-" + UUID.randomUUID().toString();
        testEventIds.add(eventId); 
        Event testEvent = new Event(eventId, "organizer-id", "Test Event for Leaving",
                "Desc", new UserList(), null, null, "Location", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), null, "qr", false, "");

        Entrant entrantUser = new Entrant(testUser.getId(), testUser.getName(), testUser.getRole(), testUser.getEmail(), testUser.getPhone(), testUser.getProfileImageUrl(),testUser.getPassword(), testUser.getNotificationPreferences(), testUser.getDevice(), testUser.getGeoPoint());
        testEvent.addEntrantWaitingList(entrantUser);

        Tasks.await(db.collection("events").document(eventId).set(testEvent));

        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(3000); 

        onData(anything())
                .inAdapterView(withId(R.id.entrant_events))
                .atPosition(0)
                .onChildView(withId(R.id.leave_button))
                .perform(click());

        Thread.sleep(2000); 

        DocumentSnapshot updatedEventDoc = Tasks.await(db.collection("events").document(eventId).get());
        Event updatedEvent = updatedEventDoc.toObject(Event.class);
        assertNotNull(updatedEvent);

        boolean isUserInWaitingList = updatedEvent.getWaitingList().getUsers().stream()
                .anyMatch(entrant -> entrant.getId().equals(testUser.getId()));

        assertFalse("Test user should NOT be in the waiting list after leaving.", isUserInWaitingList);
    }

    /**
     * Tests filtering events by the "Party" category on the home screen.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testFilterEventsByCategory() throws Exception {
        createAndLoginTestUser();
        String partyEventId = "filter-test-party-" + UUID.randomUUID().toString();
        Event partyEvent = new Event(partyEventId, "org", "Party Night", "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "Party");
        Tasks.await(db.collection("events").document(partyEventId).set(partyEvent));
        testEventIds.add(partyEventId);

        String concertEventId = "filter-test-concert-" + UUID.randomUUID().toString();
        Event concertEvent = new Event(concertEventId, "org", "Rock Concert", "desc", new UserList(10), null, null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "Concert");
        Tasks.await(db.collection("events").document(concertEventId).set(concertEvent));
        testEventIds.add(concertEventId);

        onView(withId(R.id.nav_events)).perform(click()); 
        Thread.sleep(1000);
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000); 

        onView(withId(R.id.party_button)).perform(click());
        Thread.sleep(1000); 

        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));

        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.event_name))
                .check(matches(withText("Party Night")));

        onView(withId(R.id.clear_filters_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.events_list)).check(matches(hasChildCount(2)));
    }

    /**
     * Tests filtering events by location on the home screen.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testFilterEventsByLocation() throws Exception {
        createAndLoginTestUser();
        String edmontonEventId = "loc-test-edm-" + UUID.randomUUID().toString();
        Event edmontonEvent = new Event(edmontonEventId, "org", "Edmonton Expo", "desc", new UserList(10), null, null, "Edmonton", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(edmontonEventId).set(edmontonEvent));
        testEventIds.add(edmontonEventId);

        String calgaryEventId = "loc-test-cal-" + UUID.randomUUID().toString();
        Event calgaryEvent = new Event(calgaryEventId, "org", "Calgary Stampede", "desc", new UserList(10), null, null, "Calgary", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(calgaryEventId).set(calgaryEvent));
        testEventIds.add(calgaryEventId);

        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.locationButton)).perform(click());
        onView(withClassName(Matchers.equalTo("android.widget.EditText"))).perform(typeText("Edmonton"), closeSoftKeyboard());
        onView(withText("Confirm")).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.event_name)).check(matches(withText("Edmonton Expo")));
    }

    /**
     * Tests filtering events by date on the home screen.
     * @throws Exception if test setup fails.
     */
    @Test
    public void testFilterEventsByDate() throws Exception {
        createAndLoginTestUser();
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        String todayEventId = "date-test-today-" + UUID.randomUUID().toString();
        Event todayEvent = new Event(todayEventId, "org", "Today's Market", "desc", new UserList(10), null, null, "loc", today.getTime(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(todayEventId).set(todayEvent));
        testEventIds.add(todayEventId);

        String tomorrowEventId = "date-test-tmrw-" + UUID.randomUUID().toString();
        Event tomorrowEvent = new Event(tomorrowEventId, "org", "Tomorrow's Gala", "desc", new UserList(10), null, null, "loc", tomorrow.getTime(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "");
        Tasks.await(db.collection("events").document(tomorrowEventId).set(tomorrowEvent));
        testEventIds.add(tomorrowEventId);

        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.timeButton)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH) + 1, tomorrow.get(Calendar.DAY_OF_MONTH)));
        onView(withText("OK")).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));
        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).onChildView(withId(R.id.event_name)).check(matches(withText("Tomorrow's Gala")));
    }

    /**
     * A simple test to verify the application context.
     */
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.jackpot", appContext.getPackageName());
    } 
}
