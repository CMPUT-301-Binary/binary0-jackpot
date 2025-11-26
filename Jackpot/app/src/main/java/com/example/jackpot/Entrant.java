package com.example.jackpot;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Entrant extends User {
    //private boolean notificationsOptOut;
    private List<String> historyEventIds; //Should be in database

    public Entrant(String id, String name, Role role, String email, String phone, String profileImageUrl, String password, String notificationPreferences, Device device, GeoPoint geoPoint) {
        super(id, name, role, email, phone, profileImageUrl, password, notificationPreferences, device, geoPoint);
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
        if (event.entrantInWaitingList(this)) {
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
        if (event.entrantInWaitingList(this)) {
            event.removeEntrantWaitingList(this);
        } else {
            throw new IllegalArgumentException("Event does not have entrant");
        }
    }

    /**
     * Entrant joins an event.
     * @param eventId The event to join.
     *
     */
    public void acceptInvitation(String eventId) {
        // TODO: mark invitation as accepted
    }

    /**
     * Entrant leaves an event.
     * @param eventId The event to leave.
     */
    public void declineInvitation(String eventId) {
        // TODO: mark invitation as declined
    }
}

