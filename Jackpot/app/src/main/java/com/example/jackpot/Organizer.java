package com.example.jackpot;

import java.util.ArrayList;
import java.util.List;

public class Organizer extends User {
    private String organizationName;
    private List<String> managedEventIds;

    public Organizer(String name, String id, Role role, String email, String phone, String password, String deviceID) {
        super( "Entrant", Role.ENTRANT, email, phone, password, deviceID);
        this.managedEventIds = new ArrayList<>();
    }

    public void createEvent(Event event) {
        // TODO: save event to Firebase
    }

    public void drawLottery(String eventId) {
        // TODO: trigger LotteryService to draw winners
    }
}

