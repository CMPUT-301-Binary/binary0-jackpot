package com.example.jackpot.ui.privacy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jackpot.R;

/**
 * Fragment which opens a page, showing a short summary of the privacy policy.
 *
 * Responsibilities:
 *  - Inflate privacy policy layout and display static summary text.
 */
public class PrivacyPolicyFragment extends Fragment {

    /**
     * Inflate the privacy UI and set the policy text.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        TextView textView = root.findViewById(R.id.text_privacy);
        textView.setText("Privacy Policy:\n\nWe respect your privacy. Your data is used only to improve your experience within the app. No personal data is shared with third parties.");
        return root;
    }
}
