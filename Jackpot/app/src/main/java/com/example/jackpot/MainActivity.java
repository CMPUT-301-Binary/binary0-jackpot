package com.example.jackpot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
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
    private FDatabase fDatabase = FDatabase.getInstance();
    private User.Role currentRole = User.Role.ENTRANT;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            fDatabase.getDb().collection("users").document(uid).get()
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

                            // Handle deep link after navigation is set up
                            handleDeepLink(getIntent());
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
                //fab.setOnClickListener(v -> navController.navigate(R.id.nav_event_creation));
                fab.setOnClickListener(v -> {
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                            .build();
                    navController.navigate(R.id.nav_event_creation, null, navOptions);
                });
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

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null) return;

        Uri data = intent.getData();
        if (data != null && "jackpot".equals(data.getScheme())) {
            String path = data.getHost(); // "event"
            String eventId = data.getLastPathSegment(); // the event ID

            Log.d("DeepLink", "Received deep link: " + data.toString());
            Log.d("DeepLink", "Event ID: " + eventId);

            if ("event".equals(path) && eventId != null) {
                // Load event from database and navigate to EventDetailsActivity
                openEventDetails(eventId);
            }
        }
    }

    private void openEventDetails(String eventId) {
        // Fetch the event from Firestore
        FDatabase.getInstance().getEventById(eventId, new FDatabase.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);

                    // Pass all event data to EventDetailsActivity
                    intent.putExtra("EVENT_ID", event.getEventId());
                    intent.putExtra("EVENT_NAME", event.getName());
                    intent.putExtra("EVENT_DESCRIPTION", event.getDescription());
                    intent.putExtra("EVENT_LOCATION", event.getLocation());
                    intent.putExtra("EVENT_CATEGORY", event.getCategory());
                    intent.putExtra("EVENT_PRICE", event.getPrice());
                    intent.putExtra("EVENT_CAPACITY", event.getCapacity());

                    // Convert timestamps to milliseconds
                    if (event.getDate() != null) {
                        intent.putExtra("EVENT_DATE", event.getDate().getTime());
                    }
                    if (event.getRegOpenAt() != null) {
                        intent.putExtra("EVENT_REG_OPEN", event.getRegOpenAt().getTime());
                    }
                    if (event.getRegCloseAt() != null) {
                        intent.putExtra("EVENT_REG_CLOSE", event.getRegCloseAt().getTime());
                    }
                    if (event.getWaitingList() != null) {
                        intent.putExtra("EVENT_WAITING_COUNT", event.getWaitingList().size());
                    }

                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Failed to load event: " + error,
                            Toast.LENGTH_SHORT).show();
                    Log.e("DeepLink", "Error loading event: " + error);
                });
            }
        });
    }
}
