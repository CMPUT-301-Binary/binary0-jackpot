package com.example.jackpot;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Custom Application class for Jackpot app.
 * Initializes Firebase and disables app verification for development/grading.
 */
public class JackpotApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // NOTE: App verification is disabled for development/grading purposes.
        // This allows the app to run on any machine without SHA-1 certificate setup,
        // which is necessary for group projects where TAs and professors need to
        // run the app on their own machines.
        // For production deployment, this should be removed and proper App Check
        // should be configured with appropriate SHA-1 certificates.
        FirebaseAuth.getInstance().getFirebaseAuthSettings()
                .setAppVerificationDisabledForTesting(true);
    }
}