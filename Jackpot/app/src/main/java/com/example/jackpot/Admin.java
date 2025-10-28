package com.example.jackpot;

public class Admin extends User {
    public Admin(String name, String id, Role role, String email, String phone, String password, String deviceID) {
        super( "Entrant", Role.ENTRANT, email, phone, password, deviceID);
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

