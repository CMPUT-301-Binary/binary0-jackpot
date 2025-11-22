package com.example.jackpot;

import android.content.Context;
import android.util.Log;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
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

    @Before
    public void setUp() throws Exception {
        Intents.init();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        createAndLoginTestUser();

        Thread.sleep(4000);
    }

    private void createAndLoginTestUser() throws Exception {
        String email = "testuser-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        this.testUser = new User(uid, "Test Entrant", User.Role.ENTRANT, email, "1234567890", "","", "default", null, new GeoPoint(0.0,0.0));
        Tasks.await(db.collection("users").document(uid).set(this.testUser), 10, TimeUnit.SECONDS);
    }

    @Test
    public void testEntrantJoinsEventFromHome() throws Exception {
        QuerySnapshot eventsSnapshot = Tasks.await(db.collection("events").limit(1).get());
        assertNotNull("No events found in the database for testing.", eventsSnapshot);
        assertFalse("No events found in the database for testing.", eventsSnapshot.isEmpty());
        String eventId = eventsSnapshot.getDocuments().get(0).getId();

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
        // 1. Setup: Create an event and add the test user to its waiting list.
        String eventId = "leave-test-event-" + UUID.randomUUID().toString();
        Event testEvent = new Event(eventId, "organizer-id", "Test Event for Leaving",
                "Desc", new UserList(), "Location", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), null, "qr", false, "");

        Entrant entrantUser = new Entrant(testUser.getName(), testUser.getId(), testUser.getRole(), testUser.getEmail(), testUser.getPhone(), testUser.getProfileImageUrl(),testUser.getPassword(), testUser.getNotificationPreferences(), testUser.getDevice(), testUser.getGeoPoint());
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

        // 5. Cleanup: Delete the test event.
        Tasks.await(db.collection("events").document(eventId).delete());
    }

    @After
    public void tearDown() throws Exception {
        if (mAuth.getCurrentUser() != null) {
            Tasks.await(mAuth.getCurrentUser().delete(), 10, TimeUnit.SECONDS);
        }
        Intents.release();
    }

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.jackpot", appContext.getPackageName());
    }
}
