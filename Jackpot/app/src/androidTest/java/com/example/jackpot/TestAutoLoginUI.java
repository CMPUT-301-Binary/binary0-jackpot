package com.example.jackpot;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestAutoLoginUI {

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Before
    public void setUp() throws Exception {
        Intents.init();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        // Ensure we start logged out for a clean test.
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    private User createAndLoginTestUser() throws Exception {
        String email = "autologin-user-" + UUID.randomUUID().toString() + "@example.com";
        String password = "password123";

        // Create user in Auth, which also signs them in.
        AuthResult authResult = Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 10, TimeUnit.SECONDS);
        String uid = authResult.getUser().getUid();

        // Create user document in Firestore.
        User testUser = new User(uid, "AutoLogin User", User.Role.ENTRANT, email, "1234567890", "", "", "default", null, new GeoPoint(0.0, 0.0));
        Tasks.await(db.collection("users").document(uid).set(testUser), 10, TimeUnit.SECONDS);
        
        Thread.sleep(1000); // Allow time for login state to propagate
        return testUser;
    }

    @Test
    public void testAutoLogin_OnAppRestart() throws Exception {
        // 1. First run: Create a user, which also signs them in.
        // This simulates the user logging in for the first time.
        User testUser = createAndLoginTestUser();

        // 2. Close and re-launch the MainActivity to simulate closing and reopening the app.
        scenarioRule.getScenario().close();
        ActivityScenario.launch(MainActivity.class);

        // 3. Wait for the app to restart and process the auto-login.
        Thread.sleep(4000);

        // 4. Verification:
        // Check that the main activity's layout is displayed. This proves the LoginActivity was skipped.
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() throws Exception {
        // Cleanup: Delete the user created during the test.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            try {
                db.collection("users").document(currentUser.getUid()).delete();
                Tasks.await(currentUser.delete());
            } catch (Exception e) {
                Log.w("TestAutoLoginUI", "Could not delete test user during teardown: " + e.getMessage());
            }
        }
        Intents.release();
    }
}
