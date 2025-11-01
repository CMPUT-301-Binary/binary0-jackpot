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

public class PrivacyPolicyFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        TextView tv = new TextView(getContext());
        tv.setText("Privacy & Policy Page");
        ((ViewGroup) root).addView(tv);
        return root;
    }
}

