package com.example.jackpot.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jackpot.R;
import com.example.jackpot.User;

public class NotificationFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_notification_entrant, container, false);
//        TextView tv = new TextView(getContext());
//        tv.setText("Notifications Page");
//        ((ViewGroup) root).addView(tv);
//        return root;
        String roleName = getArguments() != null ? getArguments().getString("role") : "ENTRANT";
        User.Role role = User.Role.valueOf(roleName);

        if (roleName != null) {
            role = User.Role.valueOf(roleName);
        } else {
            // Default to ENTRANT or whatever makes sense
            role = User.Role.ENTRANT;
        }
        role = User.Role.ORGANIZER; // TESTINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG
        // Inflate correct home layout
        View root;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_notification_organizer, container, false);
                break;
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_notification_admin, container, false);
                break;
            default:
                root = inflater.inflate(R.layout.fragment_notification_entrant, container, false);
                break;
        }

        return root;
    }
}

