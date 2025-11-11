package com.example.jackpot;

import java.util.ArrayList;
import java.util.List;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: Organizer.java
 *
 * Purpose/Role:
 *   Domain model for an Organizer user. Extends User and adds organizer-specific
 *   attributes (organizationName) and responsibilities (create events, draw lotteries).
 *
 * Design Notes:
 *   - Model-layer class (MVVM/MVC). Keep Android-agnostic; delegate I/O to repositories.
 *   - Avoid storing secrets (e.g., plaintext passwords) in models; use Firebase Auth.
 *
 * Outstanding Issues / TODOs:
 *   - TODO: Implement createEvent(...) in the Repository layer (not here).
 *   - TODO: Implement drawLottery(...) via a LotteryService/Repository.
 */


/**
 * Represents an Organizer user with capabilities to manage events and perform
 * lottery draws for their events. Business logic (persistence, network) should
 * live in a Repository/Service; this class remains a plain model.
 */
public class Organizer extends User {
    private String organizationName;
    private List<String> managedEventIds;

    /**
     * Constructs a new Organizer object.
     * @param name The name of the organizer.
     * @param id The unique identifier for the organizer.
     * @param role The role of the user, which should be ORGANIZER.
     * @param email The email address of the organizer.
     * @param phone The phone number of the organizer.
     * @param password The password for the organizer's account.
     * @param notificationPreferences The notification preferences for the organizer.
     * @param device The device associated with the organizer.
     */
    public Organizer(String name, String id, Role role, String email, String phone, String profileImageUrl, String password, String notificationPreferences, Device device) {
        super(id, name, Role.ORGANIZER, email, phone, profileImageUrl, password, notificationPreferences, device);
        this.managedEventIds = new ArrayList<>();
    }

    /**
     * Creates a new event.
     * @param event The event to create.
     */
    public void createEvent(Event event) {
        // TODO: save event to Firebase
    }

    /**
     * Draws the lottery for a specific event.
     * @param eventId The ID of the event for which to draw the lottery.
     */
    public void drawLottery(String eventId) {
        // TODO: trigger LotteryService to draw winners
    }
}
