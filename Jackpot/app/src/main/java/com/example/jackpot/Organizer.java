package com.example.jackpot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Organizer extends User {
    private String organizationName;
    private List<String> managedEventIds;

    public Organizer(String name, String id, Role role, String email, String phone, String password, String notificationPreferences, Device device) {
        super(id, "TestOrganizer",Role.ORGANIZER, email, phone, password, notificationPreferences, device);
        this.managedEventIds = new ArrayList<>();
    }

    public void createEvent(Event event) {
        // TODO: save event to Firebase
    }

    public void drawLottery(String eventId) {
        // TODO: trigger LotteryService to draw winners
    }
}

