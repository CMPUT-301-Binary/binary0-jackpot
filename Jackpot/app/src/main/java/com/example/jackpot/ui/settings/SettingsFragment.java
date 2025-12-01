package com.example.jackpot.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jackpot.R;

/**
 * The fragment which will display the settings page.
 *
 * Responsibilities:
 *  - Inflate the settings layout (currently static placeholder).
 */
public class SettingsFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
