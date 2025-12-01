package com.example.jackpot.ui.map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jackpot.Event;
import com.example.jackpot.FDatabase;
import com.example.jackpot.R;
import com.example.jackpot.User;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

/**
 * MapDetailFragment using osmdroid to display user locations on OpenStreetMap.
 *
 * Responsibilities:
 *  - Load event details and waiting list user locations.
 *  - Fetch full user records for geo points and render markers.
 *  - Provide back navigation and default map fallback when no data.
 */
public class MapDetailFragment extends Fragment {

    private static final String TAG = "MapDetailFragment";
    private static final String ARG_EVENT_ID = "EVENT_ID";
    private static final String ARG_EVENT_NAME = "EVENT_NAME";

    private String eventId;
    private String eventName;

    private TextView eventTitleText;
    private TextView userCountText;

    private FDatabase fDatabase;
    private Event currentEvent;

    private MapView mapView;
    private IMapController mapController;

    public MapDetailFragment() {}

    public static MapDetailFragment newInstance(String eventId, String eventName) {
        MapDetailFragment fragment = new MapDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Read arguments and prime dependencies/configuration.
     * @param savedInstanceState saved state bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }

        fDatabase = FDatabase.getInstance();

        // Configure osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
    }

    /**
     * Inflate the map UI, set up osmdroid MapView, and kick off data loading.
     * @param inflater layout inflater.
     * @param container optional parent container.
     * @param savedInstanceState saved state bundle.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map_detail, container, false);

        // Initialize views
        eventTitleText = root.findViewById(R.id.map_event_title);
        userCountText = root.findViewById(R.id.map_user_count);
        eventTitleText.setText(eventName != null ? eventName : "Event Details");

        // Initialize osmdroid MapView
        mapView = root.findViewById(R.id.map_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        mapController = mapView.getController();
        mapController.setZoom(13.0);

        // Set default center (Edmonton)
        GeoPoint startPoint = new GeoPoint(53.5461, -113.4938);
        mapController.setCenter(startPoint);

        // Back button
        View backButton = root.findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        // Load event and user locations
        loadEventAndUserLocations();

        return root;
    }

    /** Loads event info, then loads user locations. */
    private void loadEventAndUserLocations() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Event ID is null or empty");
            return;
        }

        Log.d(TAG, "Loading event details for ID: " + eventId);

        fDatabase.getEventById(eventId, new FDatabase.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (!isAdded() || event == null) return;

                currentEvent = event;
                Log.d(TAG, "Event loaded: " + currentEvent.getName());

                if (event.getWaitingList() != null &&
                        event.getWaitingList().getUsers() != null &&
                        !event.getWaitingList().getUsers().isEmpty()) {

                    ArrayList<User> waitingListUsers = event.getWaitingList().getUsers();
                    Log.d(TAG, "Found " + waitingListUsers.size() + " users in waiting list");
                    loadUsersFromFirestore(waitingListUsers);

                } else {
                    if (isAdded()) {
                        userCountText.setText("0 users with locations");
                        Toast.makeText(getContext(),
                                "No users on waiting list",
                                Toast.LENGTH_SHORT).show();
                        showDefaultMap();
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded()) return;

                Log.e(TAG, "Failed to load event: " + error);
                Toast.makeText(getContext(),
                        "Failed to load event: " + error,
                        Toast.LENGTH_SHORT).show();
                showDefaultMap();
            }
        });
    }

    /** Fetches full user data from Firestore for all users in waiting list.
     * @param waitingListUsers users from waiting list to resolve.
     */
    private void loadUsersFromFirestore(ArrayList<User> waitingListUsers) {
        if (waitingListUsers == null || waitingListUsers.isEmpty()) {
            showDefaultMap();
            return;
        }

        ArrayList<User> usersWithLocations = new ArrayList<>();
        final int totalUsers = waitingListUsers.size();
        final int[] loadedCount = {0};

        for (User waitingUser : waitingListUsers) {
            if (waitingUser.getId() == null) {
                loadedCount[0]++;
                continue;
            }

            fDatabase.getUserById(waitingUser.getId(), new FDatabase.DataCallback<User>() {
                @Override
                public void onSuccess(ArrayList<User> userData) {
                    if (!isAdded()) return;

                    if (!userData.isEmpty()) {
                        User fullUser = userData.get(0);

                        if (fullUser.getGeoPoint() != null) {
                            double lat = fullUser.getGeoPoint().getLatitude();
                            double lon = fullUser.getGeoPoint().getLongitude();

                            // Only add if coordinates are valid (not 0,0)
                            if (lat != 0.0 || lon != 0.0) {
                                usersWithLocations.add(fullUser);
                                Log.d(TAG, "User " + fullUser.getName() +
                                        " has location: (" + lat + ", " + lon + ")");
                            }
                        }
                    }

                    loadedCount[0]++;

                    // When all users are loaded, display them on the map
                    if (loadedCount[0] == totalUsers) {
                        if (isAdded()) {
                            displayUsersOnMap(usersWithLocations, totalUsers);
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (!isAdded()) return;

                    Log.e(TAG, "Failed to load user: " + waitingUser.getId(), e);
                    loadedCount[0]++;

                    // When all users are processed, display results
                    if (loadedCount[0] == totalUsers) {
                        if (isAdded()) {
                            displayUsersOnMap(usersWithLocations, totalUsers);
                        }
                    }
                }
            });
        }
    }

    /**
     * Add markers and zoom to bounding box showing all user locations.
     * @param usersWithLocations users who have valid geo points.
     * @param totalUsers total users processed from waiting list.
     */
    private void displayUsersOnMap(ArrayList<User> usersWithLocations, int totalUsers) {
        if (mapView == null) {
            Log.e(TAG, "MapView is null, cannot display users");
            return;
        }

        int usersWithValidLocations = usersWithLocations.size();

        // Update user count text
        userCountText.setText(usersWithValidLocations + " of " + totalUsers + " users with locations");

        if (usersWithValidLocations == 0) {
            Toast.makeText(getContext(),
                    "No users have shared their location",
                    Toast.LENGTH_SHORT).show();
            showDefaultMap();
            return;
        }

        // Clear existing overlays
        mapView.getOverlays().clear();

        // Variables for bounding box calculation
        double north = -90;
        double south = 90;
        double east = -180;
        double west = 180;

        // Add markers for each user
        for (User user : usersWithLocations) {
            double lat = user.getGeoPoint().getLatitude();
            double lon = user.getGeoPoint().getLongitude();

            GeoPoint point = new GeoPoint(lat, lon);

            // Update bounding box
            north = Math.max(north, lat);
            south = Math.min(south, lat);
            east = Math.max(east, lon);
            west = Math.min(west, lon);

            // Create marker
            Marker marker = new Marker(mapView);
            marker.setPosition(point);

            // Set marker title (prefer name over email)
            String userName = user.getName();
            if (userName == null || userName.isEmpty()) {
                userName = user.getEmail();
            }
            if (userName == null || userName.isEmpty()) {
                userName = "Anonymous User";
            }

            marker.setTitle(userName);
            marker.setSnippet("Waiting List User");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            mapView.getOverlays().add(marker);

            Log.d(TAG, "Added marker for user: " + userName +
                    " at (" + lat + ", " + lon + ")");
        }

        // Refresh the map to show markers
        mapView.invalidate();

        // Zoom to show all markers with a delay to ensure map is ready
        if (usersWithValidLocations > 0) {
            // Add padding to the bounding box
            double latPadding = Math.max((north - south) * 0.2, 0.01); // At least 0.01 degrees
            double lonPadding = Math.max((east - west) * 0.2, 0.01);

            final BoundingBox boundingBox = new BoundingBox(
                    north + latPadding,
                    east + lonPadding,
                    south - latPadding,
                    west - lonPadding
            );

            // Use postDelayed to ensure the map view is fully laid out
            mapView.postDelayed(() -> {
                if (mapView != null && isAdded()) {
                    try {
                        // For single marker, just center and zoom
                        if (usersWithValidLocations == 1) {
                            GeoPoint center = new GeoPoint(
                                    usersWithLocations.get(0).getGeoPoint().getLatitude(),
                                    usersWithLocations.get(0).getGeoPoint().getLongitude()
                            );
                            mapController.setCenter(center);
                            mapController.setZoom(15.0);
                        } else {
                            // For multiple markers, zoom to bounding box
                            mapView.zoomToBoundingBox(boundingBox, true, 100);
                        }
                        mapView.invalidate();
                        Log.d(TAG, "Camera adjusted to show " + usersWithValidLocations + " markers");
                    } catch (Exception e) {
                        Log.e(TAG, "Error zooming to bounding box", e);
                        // Fallback: just center on first user
                        GeoPoint fallback = new GeoPoint(
                                usersWithLocations.get(0).getGeoPoint().getLatitude(),
                                usersWithLocations.get(0).getGeoPoint().getLongitude()
                        );
                        mapController.setCenter(fallback);
                        mapController.setZoom(13.0);
                        mapView.invalidate();
                    }
                }
            }, 300); // 300ms delay
        }
    }

    /**
     * Shows default map centered on Edmonton when no users have locations.
     */
    private void showDefaultMap() {
        if (mapView != null && isAdded()) {
            GeoPoint edmonton = new GeoPoint(53.5461, -113.4938);
            mapController.setZoom(11.0);
            mapController.setCenter(edmonton);
            mapView.invalidate();
            Log.d(TAG, "Showing empty map at default location");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
