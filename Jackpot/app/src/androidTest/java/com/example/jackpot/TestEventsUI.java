package com.example.jackpot;

import android.content.Context;
import android.util.Log;

import static androidx.test.espresso.Espresso.onData;
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
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

        // Disable reCAPTCHA for testing, as seen in SignupEntrantActivity
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        createAndLoginTestUser();

        // Allow time for the HomeFragment to fetch the user and load events.
        // In a production-level test suite, this should be replaced with an IdlingResource.
        Thread.sleep(4000);
    }

    private void createAndLoginTestUser() throws Exception {
        String email = "testuser-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        // 1. Create user in Firebase Auth (logic from SignupEntrantActivity)
        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        // 2. Create user document in Firestore (logic from SignupEntrantActivity)
        this.testUser = new User(uid, "Test Entrant", User.Role.ENTRANT, email, "1234567890", "", "default", null);
        Tasks.await(db.collection("users").document(uid).set(this.testUser), 10, TimeUnit.SECONDS);
    }

    @Test
    public void testEntrantJoinsEventFromHome() throws Exception {
        // We need the ID of the event we're about to click.
        // Let's fetch the first event from the database to get its ID,
        // assuming the adapter and this query have the same ordering.
        QuerySnapshot eventsSnapshot = Tasks.await(db.collection("events").limit(1).get());
        assertNotNull("No events found in the database for testing.", eventsSnapshot);
        assertFalse("No events found in the database for testing.", eventsSnapshot.isEmpty());
        String eventId = eventsSnapshot.getDocuments().get(0).getId();

        // With a user now logged in, HomeFragment will load events.
        // Find the first event in the list and click its "Join" button.
        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0) // Get the first item
                .onChildView(withId(R.id.join_button)) // Find the button within that item
                .perform(click());

        // Give Firestore time to process the update.
        Thread.sleep(2000);

        // Verify that the entrant is now in the event's waiting list in the database.
        DocumentSnapshot updatedEventDoc = Tasks.await(db.collection("events").document(eventId).get());
        assertTrue("Event document could not be found after joining.", updatedEventDoc.exists());
        Event updatedEvent = updatedEventDoc.toObject(Event.class);
        assertNotNull("Event object could not be deserialized.", updatedEvent);
        assertNotNull("Waiting list is null.", updatedEvent.getWaitingList());

        // Check if our test user is in the waiting list
        boolean isUserInWaitingList = updatedEvent.getWaitingList().getUsers().stream()
                .anyMatch(entrant -> entrant.getId().equals(testUser.getId()));

        assertTrue("Test user was not found in the waiting list after clicking join.", isUserInWaitingList);

        // Verify that clicking "Join" does NOT start the EventDetailsActivity.
        // This confirms the button's OnClickListener is correctly decoupled from the view's OnClickListener.
        intended(not(hasComponent(EventDetailsActivity.class.getName())));
    }

    @After
    public void tearDown() throws Exception {
        // Delete the user from Firebase to keep the database clean for the next test run.
        if (mAuth.getCurrentUser() != null) {
            Tasks.await(mAuth.getCurrentUser().delete(), 10, TimeUnit.SECONDS);
        }
        Intents.release();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.jackpot", appContext.getPackageName());
    }
}
