package com.example.jackpot.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.jackpot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * The fragment which will display the settings page.
 *
 * Responsibilities:
 *  - Inflate the settings layout.
 *  - Allow entrants to opt out of organizer/admin notifications.
 */
public class SettingsFragment extends Fragment {

    private SwitchCompat entrantOptOutSwitch;
    private LinearLayout entrantOptOutCard;
    private boolean isApplyingSwitchState = false;
    private ListenerRegistration userListener;

    /**
     * Inflate the settings UI.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        entrantOptOutSwitch = root.findViewById(R.id.switch_entrant_opt_out);
        entrantOptOutCard = root.findViewById(R.id.entrant_notifications_card);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            attachUserListener(currentUser.getUid());
        } else {
            Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show();
        }

        entrantOptOutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isApplyingSwitchState) {
                return; // Ignore programmatic updates
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "Unable to update preference: not signed in.", Toast.LENGTH_SHORT).show();
                revertSwitch(buttonView);
                return;
            }
            updateOptOutPreference(user.getUid(), isChecked);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
        entrantOptOutSwitch = null;
        entrantOptOutCard = null;
    }

    private void attachUserListener(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userListener = db.collection("users").document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Failed to load settings", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(requireContext(), "User profile missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Object roleObj = snapshot.get("role");
                    String role = roleObj != null ? roleObj.toString() : null;
                    Boolean optOut = snapshot.getBoolean("notificationsOptOut");

                    boolean isEntrant = "ENTRANT".equals(role);
                    entrantOptOutCard.setVisibility(isEntrant ? View.VISIBLE : View.GONE);

                    if (isEntrant && entrantOptOutSwitch != null) {
                        isApplyingSwitchState = true;
                        entrantOptOutSwitch.setChecked(Boolean.TRUE.equals(optOut));
                        isApplyingSwitchState = false;
                    }
                });
    }

    private void updateOptOutPreference(String userId, boolean optOut) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("notificationsOptOut", optOut)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(requireContext(),
                                optOut ? "Opted out of organizer/admin notifications" :
                                        "Notifications re-enabled",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    revertSwitch(entrantOptOutSwitch);
                    Toast.makeText(requireContext(),
                            "Failed to update preference: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void revertSwitch(android.widget.CompoundButton switchCompat) {
        if (switchCompat == null) {
            return;
        }
        isApplyingSwitchState = true;
        switchCompat.toggle();
        isApplyingSwitchState = false;
    }
}
