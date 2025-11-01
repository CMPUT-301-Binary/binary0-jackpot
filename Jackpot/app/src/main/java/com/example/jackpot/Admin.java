package com.example.jackpot;

import java.util.UUID;

public class Admin extends User {
    public Admin(String name, String id, Role role, String email, String phone, String password, String notificationPreferences, Device device) {
        super(id, "TestAdmin",Role.ADMIN, email, phone, password, notificationPreferences, device);
    }

    public void removeEvent(String eventId) {
        // TODO: delete event from Firebase
    }

    public void removeProfile(String userId) {
        // TODO: delete user from Firebase
    }

    public void removeImage(String imageId) {
        // TODO: delete image
    }
}

