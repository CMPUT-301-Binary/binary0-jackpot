package com.example.jackpot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Entrant extends User {
    //private boolean notificationsOptOut;
    private List<String> historyEventIds; //Should be in database

    public Entrant(String name, UUID id, Role role, String email, String phone, String password, String notificationPreferences, Device device) {
        super(UUID.randomUUID(), "TestEntrant",Role.ENTRANT, email, phone, password, notificationPreferences, device);
        //this.notificationsOptOut = false;
        this.historyEventIds = new ArrayList<>();
    }

    public void joinWaitingList(String eventId) {
        // TODO: add entrant to waiting list
    }

    public void leaveWaitingList(String eventId) {
        // TODO: remove entrant from waiting list
    }

    public void acceptInvitation(String eventId) {
        // TODO: mark invitation as accepted
    }

    public void declineInvitation(String eventId) {
        // TODO: mark invitation as declined
    }
}

