package com.example.jackpot.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.MainActivity;
import com.example.jackpot.R;
import com.example.jackpot.User;

/**
 * Notification fragment, which will show the notifications of the user.
 */
public class NotificationFragment extends Fragment {
    /**
     * Called to have the fragment instantiate its user interface view.
     * This checks the role of the user, to determine what the page should look
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
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
        //User.Role role = User.Role.valueOf(roleName);
        User.Role role = ((MainActivity) requireActivity()).getCurrentUserRole();

//        if (roleName != null) {
//            role = User.Role.valueOf(roleName);
//        } else {
//            // Default to ENTRANT or whatever makes sense
//            role = User.Role.ENTRANT;
//        }
//        role = User.Role.ORGANIZER; // TESTINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG

        if (role == null){
            role = User.Role.ENTRANT;
        }
        // Inflate correct home layout
        View root;
        int notificationLayoutResource;
                switch (role) {
                    //NOTE: Possibly change the notificationLayerResource for the different types of users.
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_notification_organizer, container, false);
                notificationLayoutResource = R.layout.notification_event_item;
                break;
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_notification_admin, container, false);
                notificationLayoutResource = R.layout.notification_event_item;
                break;
            default:
                root = inflater.inflate(R.layout.fragment_notification_entrant, container, false);
                notificationLayoutResource = R.layout.notification_event_item;
                break;
        }

        return root;
    }
}

