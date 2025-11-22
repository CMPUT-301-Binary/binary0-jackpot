package com.example.jackpot;

import android.util.Log;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.jackpot.activities.ui.LoginActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestProfileUI {
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
        Thread.sleep(1000); 
    }

    @Test
    public void testUpdateProfile_AndDataIsSaved() throws Exception {
        createAndLoginTestUser();

        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(2000);

        String newName = "John Smith";
        String newPhone = "5551234567";

        onView(withId(R.id.profile_name)).perform(clearText(), typeText(newName), closeSoftKeyboard());
        onView(withId(R.id.profile_phone)).perform(clearText(), typeText(newPhone), closeSoftKeyboard());
        onView(withId(R.id.save_profile_button)).perform(click());

        Thread.sleep(3000); 

        DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(testUser.getId()).get());
        assertTrue("User document does not exist in Firestore.", userDoc.exists());

        assertEquals("Name was not updated in Firestore.", newName, userDoc.getString("name"));
        assertEquals("Phone was not updated in Firestore.", newPhone, userDoc.getString("phone"));
    }

    @Test
    public void testDeleteProfile_AndDataIsRemoved() throws Exception {
        // 1. Create and log in a user.
        createAndLoginTestUser();
        String uid = testUser.getId(); // Save the UID for later verification.

        // 2. Navigate to the Profile Fragment.
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(2000); // Wait for profile to load.

        // 3. UI Interaction: Click delete button and confirm in the dialog.
        onView(withId(R.id.delete_account_button)).perform(click());
        
        // The confirmation dialog appears. Click the positive "Delete" button.
        onView(withText("Delete")).perform(click());

        // 4. Wait for async operations (Auth and Firestore deletion) to complete.
        Thread.sleep(5000);

        // 5. Verification
        // Verify that the app navigated to the LoginActivity.
        intended(hasComponent(LoginActivity.class.getName()));

        // Verify the user's document was deleted from Firestore.
        DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(uid).get());
        assertFalse("User document should have been deleted from Firestore, but it still exists.", userDoc.exists());
    }


    @After
    public void tearDown() throws Exception {
        if (mAuth.getCurrentUser() != null) {
            try {
                Tasks.await(mAuth.getCurrentUser().delete());
            } catch (Exception e) {
                Log.w("TestCleanup", "Could not delete test user: " + e.getMessage());
            }
        }
        Intents.release();
    }
}
