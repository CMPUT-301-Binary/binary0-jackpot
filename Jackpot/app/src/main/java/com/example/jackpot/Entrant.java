package com.example.jackpot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Entrant extends User {
    //private boolean notificationsOptOut;
    private List<String> historyEventIds; //Should be in database

    public Entrant(String name, String id, Role role, String email, String phone, String password, String notificationPreferences, Device device) {
        super(id, name, role, email, phone, password, notificationPreferences, device);
        //this.notificationsOptOut = false;
        this.historyEventIds = new ArrayList<>();
    }
    /**
     * Entrant joins a waiting list for an event.
     * @param event The event to join the waiting list for.
     * @throws NullPointerException If event is null.
     * @throws IllegalArgumentException If event already has entrant.
     */
    public void joinWaitingList(Event event) {
        if (event==null) {
            throw new NullPointerException("Event is null");
        }
        if (event.hasEntrant(this)) {
            throw new IllegalArgumentException("Event already has entrant");
        }
        event.addEntrantWaitingList(this);
    }

    /**
     * Entrant leaves a waiting list for an event.
     * @param event The event to leave the waiting list.
     * @throws NullPointerException If event is null.
     * @throws IllegalArgumentException If event does not have entrant.
     */
    public void leaveWaitingList(Event event) {
        if (event==null) {
            throw new NullPointerException("Event is null");
        }
        if (event.hasEntrant(this)) {
            event.removeEntrantWaitingList(this);
        } else {
            throw new IllegalArgumentException("Event does not have entrant");
        }
    }

    public void acceptInvitation(String eventId) {
        // TODO: mark invitation as accepted
    }

    public void declineInvitation(String eventId) {
        // TODO: mark invitation as declined
    }
}

