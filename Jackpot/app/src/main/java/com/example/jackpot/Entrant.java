package com.example.jackpot;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user with the Entrant role.
 * Extends the base {@link User} class with functionalities specific to event attendees,
 * such as joining and leaving event waiting lists.
 */
public class Entrant extends User {
    //private boolean notificationsOptOut;
    private List<String> historyEventIds; //Should be in database

    /**
     * Constructs a new Entrant instance.
     *
     * @param id The unique identifier for the user.
     * @param name The full name of the user.
     * @param role The user's role (should be ENTRANT).
     * @param email The user's email address.
     * @param phone The user's phone number.
     * @param profileImageUrl The URL for the user's profile image.
     * @param password The user's password.
     * @param notificationPreferences User's notification settings.
     * @param device The user's device information.
     * @param geoPoint The user's last known location.
     */
    public Entrant(String id, String name, Role role, String email, String phone, String profileImageUrl, String password, String notificationPreferences, Device device, GeoPoint geoPoint) {
        super(id, name, role, email, phone, profileImageUrl, password, notificationPreferences, device, geoPoint);
        //this.notificationsOptOut = false;
        this.historyEventIds = new ArrayList<>();
    }
    /**
     * Allows the entrant to join the waiting list for a given event.
     * Throws an exception if the entrant is already on the list.
     *
     * @param event The event whose waiting list is to be joined.
     * @throws NullPointerException If the provided event is null.
     * @throws IllegalArgumentException If the entrant is already on the waiting list.
     */
    public void joinWaitingList(Event event) {
        if (event==null) {
            throw new NullPointerException("Event is null");
        }
        // Allow rejoining if previously cancelled; block other list memberships.
        if (event.entrantInList(id, event.getWaitingList())
                || event.entrantInList(id, event.getInvitedList())
                || event.entrantInList(id, event.getJoinedList())) {
            throw new IllegalArgumentException("Event already has entrant");
        }
        event.addEntrantWaitingList(this);
    }

    /**
     * Allows the entrant to leave the waiting list for a given event.
     *
     * @param event The event whose waiting list is to be left.
     * @throws NullPointerException If the provided event is null.
     * @throws IllegalArgumentException If the entrant is not on the waiting list.
     */
    public void leaveWaitingList(Event event) {
        if (event==null) {
            throw new NullPointerException("Event is null");
        }
        if (event.hasEntrant(id)) {
            event.removeEntrantWaitingList(this);
        } else {
            throw new IllegalArgumentException("Event does not have entrant");
        }
    }

    /**
     * Marks an invitation for a given event as accepted by the entrant.
     * @param eventId The ID of the event for which the invitation is accepted.
     */
    public void acceptInvitation(String eventId) {
        // TODO: mark invitation as accepted
    }

    /**
     * Marks an invitation for a given event as declined by the entrant.
     * @param eventId The ID of the event for which the invitation is declined.
     */
    public void declineInvitation(String eventId) {
        // TODO: mark invitation as declined
    }
}
