package com.example.jackpot;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.jackpot.ui.image.Image;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented tests for administrator functionalities.
 * This class tests core admin user stories, including browsing and deleting profiles,
 * events, and images.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestAdminUI {

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final List<String> testEventIds = new ArrayList<>();
    private final List<String> testUserIds = new ArrayList<>();

    /**
     * Sets up the test environment before each test.
     * Initializes Firebase instances and signs out any existing user to ensure a clean slate.
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
     * Helper method to create a new, unique administrator user in Firebase Auth and Firestore,
     * then signs them in for the test.
     * @throws Exception if user creation or login fails.
     */
    private void createAndLoginAdmin() throws Exception {
        String email = "admin-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        User testAdmin = new User(uid, "Test Admin", User.Role.ADMIN, email, "1112223333", "", "", "default", null, new GeoPoint(0.0, 0.0));
        Tasks.await(db.collection("users").document(uid).set(testAdmin), 10, TimeUnit.SECONDS);
        testUserIds.add(uid);

        Thread.sleep(2000);
    }

    /**
     * Tests if an administrator can successfully navigate to the profile browsing screen
     * and see a list of non-admin users.
     * @throws Exception if test setup or execution fails.
     */
    @Test
    public void testAdminCanBrowseProfiles() throws Exception {
        // 1. Log in as an Administrator
        createAndLoginAdmin();
        scenarioRule.getScenario().close();
        ActivityScenario.launch(MainActivity.class);
        Thread.sleep(3000);

        // 2. Create two non-admin users for browsing
        String entrantId = "browse-entrant-" + UUID.randomUUID();
        User entrantUser = new User(entrantId, "Browser Entrant", User.Role.ENTRANT, "browse-e@test.com", "", "", "", "", null, null);
        Tasks.await(db.collection("users").document(entrantId).set(entrantUser));
        testUserIds.add(entrantId);

        String organizerId = "browse-organizer-" + UUID.randomUUID();
        User organizerUser = new User(organizerId, "Browser Organizer", User.Role.ORGANIZER, "browse-o@test.com", "", "", "", "", null, null);
        Tasks.await(db.collection("users").document(organizerId).set(organizerUser));
        testUserIds.add(organizerId);

        // 3. Navigate to the profile list screen
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(4000); // Wait for the list to load

        // 4. Verification: Check that the RecyclerView is displayed and contains the two users.
        onView(withId(R.id.profiles_recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.profiles_recycler_view)).check(matches(hasChildCount(2)));

        // Verify that both created users are visible by checking for their names
        onView(withId(R.id.profiles_recycler_view))
                .check(matches(hasDescendant(withText("Browser Entrant"))));
        onView(withId(R.id.profiles_recycler_view))
                .check(matches(hasDescendant(withText("Browser Organizer"))));
    }

    /**
     * Tests if an administrator can delete multiple organizer profiles sequentially.
     * Verifies that the UI updates correctly after each deletion and confirms that the
     * corresponding user documents are removed from Firestore while non-targeted users remain.
     * @throws Exception if test setup or execution fails.
     */
    @Test
    public void testAdminCanRemoveProfiles() throws Exception {
        // 1. Log in as an admin
        createAndLoginAdmin();
        scenarioRule.getScenario().close();
        ActivityScenario.launch(MainActivity.class);
        Thread.sleep(3000);

        // 2. Setup: Create two ORGANIZERs to delete and one ENTRANT to keep.
        String orgToDeleteId1 = "delete-org-1-" + UUID.randomUUID();
        User orgToDelete1 = new User(orgToDeleteId1, "Organizer ToDelete 1", User.Role.ORGANIZER, "delete-org1@test.com", "", "", "", "", null, null);
        Tasks.await(db.collection("users").document(orgToDeleteId1).set(orgToDelete1));
        testUserIds.add(orgToDeleteId1);

        String orgToDeleteId2 = "delete-org-2-" + UUID.randomUUID();
        User orgToDelete2 = new User(orgToDeleteId2, "Organizer ToDelete 2", User.Role.ORGANIZER, "delete-org2@test.com", "", "", "", "", null, null);
        Tasks.await(db.collection("users").document(orgToDeleteId2).set(orgToDelete2));
        testUserIds.add(orgToDeleteId2);

        String entrantToKeepId = "keep-entrant-" + UUID.randomUUID();
        User entrantToKeep = new User(entrantToKeepId, "Entrant ToKeep", User.Role.ENTRANT, "keep-entrant@test.com", "", "", "", "", null, null);
        Tasks.await(db.collection("users").document(entrantToKeepId).set(entrantToKeep));
        testUserIds.add(entrantToKeepId);

        // 3. Navigate to the profile browsing screen
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(4000); // Wait for users to load

        // 4. Verify all three non-admin users are displayed
        onView(withId(R.id.profiles_recycler_view)).check(matches(hasChildCount(3)));

        // 5. Action: Select the first organizer by name and click delete
        onView(withId(R.id.profiles_recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Organizer ToDelete 1")), click()));
        onView(withId(R.id.button_delete_profile)).perform(click());
        Thread.sleep(3000); // Wait for deletion

        // 6. Verification after first deletion
        onView(withId(R.id.profiles_recycler_view)).check(matches(hasChildCount(2)));
        DocumentSnapshot deletedDoc1 = Tasks.await(db.collection("users").document(orgToDeleteId1).get());
        assertFalse("First deleted organizer's document should not exist in Firestore.", deletedDoc1.exists());

        // 7. Action: Select the second organizer by name and click delete
        onView(withId(R.id.profiles_recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Organizer ToDelete 2")), click()));
        onView(withId(R.id.button_delete_profile)).perform(click());
        Thread.sleep(3000); // Wait for deletion

        // 8. Final Verification
        onView(withId(R.id.profiles_recycler_view)).check(matches(hasChildCount(1)));
        DocumentSnapshot deletedDoc2 = Tasks.await(db.collection("users").document(orgToDeleteId2).get());
        assertFalse("Second deleted organizer's document should not exist in Firestore.", deletedDoc2.exists());
        DocumentSnapshot keepDoc = Tasks.await(db.collection("users").document(entrantToKeepId).get());
        assertTrue("Entrant who was not deleted should still exist in Firestore.", keepDoc.exists());
    }

    /**
     * Tests if an administrator can browse and delete images. It seeds the database with
     * a user profile image and an event poster, navigates to the image browsing screen,
     * deletes one image, and verifies that both the UI and the database state are updated correctly.
     * @throws Exception if test setup or execution fails.
     */
    @Test
    public void testAdminCanBrowseAndRemoveImages() throws Exception {
        createAndLoginAdmin();
        scenarioRule.getScenario().close();
        ActivityScenario.launch(MainActivity.class);
        Thread.sleep(3000);

        // 1. Setup: Create a user with a profile pic and an event with a poster.
        String profileUserId = "img-user-" + UUID.randomUUID();
        String profileUrl = "https://firebasestorage.googleapis.com/v0/b/project.appspot.com/o/images%2Ftest_profile.jpg";
        User profileUser = new User(profileUserId, "Profile Pic User", User.Role.ENTRANT, "p" + UUID.randomUUID() + "@t.com", "", profileUrl, "", "", null, null);
        Tasks.await(db.collection("users").document(profileUserId).set(profileUser));
        testUserIds.add(profileUserId);

        String eventId = "img-event-" + UUID.randomUUID();
        String posterUrl = "https://firebasestorage.googleapis.com/v0/b/project.appspot.com/o/images%2Ftest_poster.jpg";
        
        // Use the correct constructor for the Image object
        String uploaderId = "org1";
        Image eventImage = new Image("poster_" + eventId, uploaderId, posterUrl, Image.TYPE_POSTER, Image.ORDER_POSTER, eventId);
        Tasks.await(db.collection("images").document(eventImage.getImageID()).set(eventImage));
        
        Event testEvent = new Event(eventId, uploaderId, "Image Event", "d", "Criteria", new UserList(10), new UserList(10), new UserList(10), null, "loc", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), posterUrl, "", false, "cat");
        Tasks.await(db.collection("events").document(eventId).set(testEvent));
        testEventIds.add(eventId);

        // 2. Navigate to the Image Browser screen using the correct ID from the admin menu.
        onView(withId(R.id.nav_image)).perform(click());
        Thread.sleep(4000); // Wait for images to load.

        // 3. Verify both images are displayed.
        onView(withId(R.id.image_recycler_view)).check(matches(hasChildCount(2)));

        // 4. Action: Select the event poster (assuming it's the first item) and delete it.
        onView(withId(R.id.image_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button_delete_image)).perform(click());

        // 5. Verification: Wait for deletion and check UI and DB.
        Thread.sleep(5000);

        onView(withId(R.id.image_recycler_view)).check(matches(hasChildCount(1)));

        DocumentSnapshot eventDoc = Tasks.await(db.collection("events").document(eventId).get());
        assertEquals("Event posterUri should be reset to 'default'.", "default", eventDoc.getString("posterUri"));

        DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(profileUserId).get());
        assertEquals("User profileImageUrl should be unchanged.", profileUrl, userDoc.getString("profileImageUrl"));
    }

    /**
     * Tests if an administrator can browse events on the home screen.
     * It seeds the database with two events and verifies they are displayed in the list.
     * @throws Exception if test setup or execution fails.
     */
    @Test
    public void testAdminCanBrowseEvents() throws Exception {
        createAndLoginAdmin();
        scenarioRule.getScenario().close();
        ActivityScenario.launch(MainActivity.class);
        Thread.sleep(3000);

        String eventId1 = "admin-test-event-1-" + UUID.randomUUID().toString();
        Event testEvent1 = new Event(eventId1, "org1", "Admin Event One", "desc1", "Criteria", new UserList(10), new UserList(10), new UserList(10), null, "loc1", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat1");
        Tasks.await(db.collection("events").document(eventId1).set(testEvent1));
        testEventIds.add(eventId1);

        String eventId2 = "admin-test-event-2-" + UUID.randomUUID().toString();
        Event testEvent2 = new Event(eventId2, "org2", "Admin Event Two", "desc2", "Criteria", new UserList(10), new UserList(10), new UserList(10), null, "loc2", new Date(), 0.0, 0.0, 0.0, 20, new Date(), new Date(), "", "", false, "cat2");
        Tasks.await(db.collection("events").document(eventId2).set(testEvent2));
        testEventIds.add(eventId2);

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.events_list)).check(matches(isDisplayed()));
        onView(withId(R.id.events_list)).check(matches(hasChildCount(2)));
    }

    /**
     * Tests if an administrator can delete an event from the event details screen.
     * It creates two events, navigates to the details of one, deletes it, and then
     * verifies that the UI list updates and the event document is removed from Firestore.
     * @throws Exception if test setup or execution fails.
     */
    @Test
    public void testAdminCanDeleteEvent() throws Exception {
        createAndLoginAdmin();
        scenarioRule.getScenario().close();
        ActivityScenario.launch(MainActivity.class);
        Thread.sleep(3000);

        String eventToDeleteId = "admin-delete-this-event-" + UUID.randomUUID().toString();
        Event eventToDelete = new Event(eventToDeleteId, "org1", "Event To Be Deleted", "desc1", "Criteria", new UserList(10), new UserList(10), new UserList(10), null, "loc1", new Date(), 0.0, 0.0, 0.0, 10, new Date(), new Date(), "", "", false, "cat1");
        Tasks.await(db.collection("events").document(eventToDeleteId).set(eventToDelete));
        testEventIds.add(eventToDeleteId);

        String eventToKeepId = "admin-keep-this-event-" + UUID.randomUUID().toString();
        Event eventToKeep = new Event(eventToKeepId, "org2", "Event To Keep", "desc2", "Criteria", new UserList(10), new UserList(10), new UserList(10), null, "loc2", new Date(), 0.0, 0.0, 0.0, 20, new Date(), new Date(), "", "", false, "cat2");
        Tasks.await(db.collection("events").document(eventToKeepId).set(eventToKeep));
        testEventIds.add(eventToKeepId);

        onView(withId(R.id.nav_home)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.events_list)).check(matches(hasChildCount(2)));

        onData(anything()).inAdapterView(withId(R.id.events_list)).atPosition(0).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.event_details_delete_button)).perform(click());
        onView(withText("Delete")).perform(click());

        Thread.sleep(3000);
        onView(withId(R.id.events_list)).check(matches(isDisplayed()));
        onView(withId(R.id.events_list)).check(matches(hasChildCount(1)));

        DocumentSnapshot deletedDoc = Tasks.await(db.collection("events").document(eventToDeleteId).get());
        assertFalse("Event document should have been deleted from Firestore, but it still exists.", deletedDoc.exists());
    }

    /**
     * Cleans up the test environment after each test by deleting all created
     * test users and events from Firebase Auth and Firestore.
     * @throws Exception if cleanup fails.
     */
    @After
    public void tearDown() throws Exception {
        for (String eventId : testEventIds) {
            try {
                db.collection("events").document(eventId).delete();
            } catch (Exception e) {
                Log.w("TestAdminUI", "Could not delete event: " + eventId, e);
            }
        }
        testEventIds.clear();

        for (String userId : testUserIds) {
            try {
                db.collection("users").document(userId).delete();
            } catch (Exception e) {
                Log.w("TestAdminUI", "Could not delete user: " + userId, e);
            }
        }
        testUserIds.clear();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            try {
                Tasks.await(currentUser.delete());
            } catch (Exception e) {
                Log.w("TestAdminUI", "Could not delete test admin user: " + e.getMessage());
            }
        }
        Intents.release();
    }
}
