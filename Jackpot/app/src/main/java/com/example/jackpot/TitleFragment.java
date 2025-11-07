/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: TitleFragment.java
 *
 * Purpose/Role:
 *  Displays the app’s title/landing screen and acts as the entry point into
 *  navigation (e.g., sign-in, role selection, or quick actions like “Scan QR” if shown).
 *  This fragment is a View-layer component (MVVM/MVC) and should contain only UI and navigation
 *  logic.
 *
 * Design Notes:
 *  - AndroidX Fragment used as a self-contained UI screen.
 *  - Navigation should be handled via the NavController (safe-args if applicable).
 *
 * Outstanding Issues / TODOs:
 *  - TODO: Remove template args (ARG_PARAM1/ARG_PARAM2) if not used anywhere.
 *  - TODO: Rename and change types of parameters
 */

package com.example.jackpot;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TitleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TitleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TitleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TitleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TitleFragment newInstance(String param1, String param2) {
        TitleFragment fragment = new TitleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes fragment-scoped state from arguments if present.
     *
     * @param savedInstanceState previously saved state, or {@code null}.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Inflates the title screen layout and returns the root view.
     * Wire any buttons here to the NavController.
     *
     * @param inflater  LayoutInflater to inflate the XML layout.
     * @param container Optional parent view.
     * @param savedInstanceState previously saved state, or {@code null}.
     * @return the root view for this fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_title, container, false);
    }
}