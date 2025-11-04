package com.example.jackpot.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.R;
import com.example.jackpot.User;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //String roleString = requireActivity().getIntent().getStringExtra("USER_ROLE");
        //User.Role currentRole = User.Role.valueOf(roleString);

        String roleName = getArguments() != null ? getArguments().getString("role") : "ENTRANT";
        User.Role role = User.Role.valueOf(roleName);

        if (roleName != null) {
            role = User.Role.valueOf(roleName);
        } else {
            // Default to ENTRANT
            role = User.Role.ENTRANT;
        }
        role = User.Role.ORGANIZER; // TESTINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG
        // Inflate correct home layout
        View root;
        switch (role) {
            case ORGANIZER:
                root = inflater.inflate(R.layout.fragment_home_organizer, container, false);
                break;
            case ADMIN:
                root = inflater.inflate(R.layout.fragment_home_admin, container, false);
                break;
            default:
                root = inflater.inflate(R.layout.fragment_home_entrant, container, false);
                break;
        }

        return root;
    }
}

