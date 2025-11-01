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
    /**
     * Entrant joins a waiting list for an event.
     * @param event The event to join the waiting list for.
     *              If the waiting list is full,
     *              the entrant is not added to the event's waiting list.
     *              If the waiting list is not full,
     *              the entrant is added to the waiting list.
     *              If the waiting list is null,
     *              throw an exception.
     *              If the waiting list is not null,
     *              but the capacity is null, the entrant is added to the waiting list.
     */
    public void joinWaitingList(Event event) {
        EntrantList waitingList = event.getWaitingList();
        if (waitingList == null) {
            throw  new NullPointerException("Waiting list is null");
        }
        if (waitingList.getCapacity() == null || waitingList.size() < waitingList.getCapacity()) {
            waitingList.add(this);
        } else {
            // TODO: handle full waiting list
            // Probably an user prompt on screen or something like that
        }
    }

    /**
     * Entrant leaves a waiting list for an event.
     * @param event The event to leave the waiting list for.
     *              If the waiting list is null,
     *              throw an exception.
     *              If the waiting list is not null,
     *              remove the entrant from the waiting list if they are in it.
     */
    public void leaveWaitingList(Event event) {
        EntrantList waitingList = event.getWaitingList();
        if (waitingList == null) {
            throw  new NullPointerException("Waiting list is null");
        }
        if (waitingList.contains(this)) {
            waitingList.remove(this);
        } else {
            // TODO: handle entrant not in waiting list
            // Probably an user prompt on screen or something like that
        }
    }

    public void acceptInvitation(String eventId) {
        // TODO: mark invitation as accepted
    }

    public void declineInvitation(String eventId) {
        // TODO: mark invitation as declined
    }
}

