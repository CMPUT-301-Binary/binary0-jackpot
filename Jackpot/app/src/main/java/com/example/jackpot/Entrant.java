package com.example.jackpot;

import java.util.ArrayList;
import java.util.List;

public class Entrant extends User {
    //private boolean notificationsOptOut;
    private List<String> historyEventIds; //Should be in database

    public Entrant(String name, String id, Role role, String email, String phone, String password, String deviceID) {
        super( "Entrant", Role.ENTRANT, email, phone, password, deviceID);
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

