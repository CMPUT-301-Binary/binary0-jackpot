package com.example.jackpot;

import com.google.firebase.firestore.GeoPoint;

import java.util.UUID;

/**
 * This class represents an Admin. An admin has the ability to remove events, profiles, and images.
 * Todo: Find a way to integrate this with the current structure we have.
 */
public class Admin extends User {
    /**
     * Constructor for Admin.
     * @param name Name of the admin.
     * @param id ID of the admin.
     * @param role Role of the admin.
     * @param email Email of the admin.
     * @param phone Phone number of the admin.
     * @param password Password of the admin.
     * @param notificationPreferences Notification preferences of the admin.
     * @param device Device of the admin.
     */
    public Admin(String name, String id, Role role, String email, String phone, String profileImageUrl, String password, String notificationPreferences, Device device, GeoPoint geoPoint) {
        super(id, "TestAdmin",Role.ADMIN, email, phone, "default", password, notificationPreferences, device, geoPoint);
    }

    /**
     * Removes an event from the system.
     * @param eventId ID of the event to be removed.
     */

    public void removeEvent(String eventId) {
        // TODO: delete event from Firebase
    }
    /**
     * Removes a profile from the system.
     * @param userId ID of the profile to be removed.
     */
    public void removeProfile(String userId) {
        // TODO: delete user from Firebase
    }

    /**
     * Removes an image from the system.
     * @param imageId ID of the image to be removed.
     */
    public void removeImage(String imageId) {
        // TODO: delete image
    }
}

