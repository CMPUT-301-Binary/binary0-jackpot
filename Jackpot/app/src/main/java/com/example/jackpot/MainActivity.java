package com.example.jackpot;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.jackpot.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseFirestore db;
    private User.Role currentRole = User.Role.ENTRANT;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        if (binding.appBarMain.fab != null) {
            binding.appBarMain.fab.hide();
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView drawerNav = binding.navView;
        BottomNavigationView bottomNav = binding.appBarMain.contentMain.bottomNavView;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null && user.getRole() != null) {
                            currentRole = user.getRole();
                            Log.d("Firestore", "Logged in as: " + currentRole);
//                            currentRole = User.Role.ORGANIZER; // TESTINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG

                            // Inflate menus based on role
                            setupMenusAndFab(currentRole, bottomNav, drawerNav);

                            // Use unified Navigation setup again
                            setupUnifiedNavigation(binding, navController, bottomNav, drawerNav);

                            // Pass role to HomeFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("role", currentRole.name());
                            navController.navigate(R.id.nav_home, bundle);
                        } else {
                            setupUnifiedNavigation(binding, navController, bottomNav, drawerNav);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading role", e);
                        setupUnifiedNavigation(binding, navController, bottomNav, drawerNav);
                    });
        } else {
            setupUnifiedNavigation(binding, navController, bottomNav, drawerNav);
        }

        //Connect the event creation button to the event creation fragment.
        binding.appBarMain.fab.setOnClickListener(v -> {
            navController.navigate(R.id.eventCreationFragment);
        });
    }

    private void setupUnifiedNavigation(ActivityMainBinding binding,
                                        NavController navController,
                                        BottomNavigationView bottomNav,
                                        NavigationView drawerNav) {
        // Combine all top-level destinations from BOTH
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_events, R.id.nav_notification, R.id.nav_profile, R.id.nav_map,
                R.id.drawer_settings, R.id.drawer_privacy_policy, R.id.drawer_location)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        // Attach toolbar
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        //Attach drawer menu
        if (drawerNav != null && drawerNav.getMenu().size() > 0) {
            NavigationUI.setupWithNavController(drawerNav, navController);
        }

        //Attach bottom navigation
        if (bottomNav != null && bottomNav.getMenu().size() > 0) {
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    private void setupMenusAndFab(User.Role role,
                                  BottomNavigationView bottomNav,
                                  NavigationView drawerNav) {

        bottomNav.getMenu().clear();
        drawerNav.getMenu().clear();

        FloatingActionButton fab = findViewById(R.id.fab);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);


        switch (role) {
            case ORGANIZER:
                bottomNav.inflateMenu(R.menu.bottom_navigation_organizer);
                drawerNav.inflateMenu(R.menu.activity_side_bar_drawer);
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
                fab.setOnClickListener(v -> navController.navigate(R.id.nav_event_creation));
                break;
            case ADMIN:
                bottomNav.inflateMenu(R.menu.bottom_navigation_admin);
                drawerNav.inflateMenu(R.menu.activity_side_bar_drawer);
                binding.appBarMain.fab.hide();
                break;
            default:
                bottomNav.inflateMenu(R.menu.bottom_navigation_entrant);
                drawerNav.inflateMenu(R.menu.activity_side_bar_drawer);
                binding.appBarMain.fab.hide();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView == null) {
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.drawer_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.drawer_settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
