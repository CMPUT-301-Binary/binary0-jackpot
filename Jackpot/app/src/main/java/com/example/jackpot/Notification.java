package com.example.jackpot;

import android.os.Build;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.util.UUID;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: Notification.java
 *
 * Purpose/Role:
 *   Plain model (POJO) representing an in-app notification to a user. Holds recipient/event
 *   references, a type string, payload, timestamps, and basic delivery state. No Android deps.
 *
 * Design Notes:
 *   - Model layer only; persistence and transport live elsewhere.
 *   - If Android’s android.app.Notification is also used, consider renaming this to AppNotification
 *     to avoid type-name collisions.
 *
 * Outstanding Issues / TODOs:
 *   - TODO: Replace notifType String with a small enum or constrained constants.
 *   - TODO: Add (de)serialization helpers (toMap()/fromMap()) if storing in Firebase.
 */

/**
 * Represents a notification to be sent to a user within the application.
 *
 * Encapsulates recipient, related event, type, payload, and basic delivery state.
 * This is a data model only; transport is handled by services/repositories.
 */
public class Notification {
    private String notificationID;
    private String recipientID;
    private String eventID;
    private String notifType;
    private String payload;
    private com.google.firebase.Timestamp sentAt;
    private boolean deliverStatus;
    private String providerMsgID;
    private String error;
    private String organizerID;
    private boolean viewedByEntrant;



    /**
     * Constructs a new Notification object, initializing it with essential details.
     * The notification ID is randomly generated, and the sent time is set to the current moment.
     *
     * @param recipientID The ID of the user who will receive the notification.
     * @param eventID The ID of the event this notification is associated with. Can be null.
     * @param notifType A string representing the type of notification (e.g., "NEW_EVENT", "WINNER_ANNOUNCEMENT").
     * @param payload The main content/message of the notification.
     */
    public Notification(String recipientID, String eventID, String notifType, String payload){
        this.notificationID = UUID.randomUUID().toString();
        this.recipientID = recipientID;
        this.eventID = eventID;
        this.notifType = notifType;
        this.payload = payload;
        this.sentAt = Timestamp.now();
        this.deliverStatus = false;
        this.viewedByEntrant = false;
    }

    /**
     * Empty constructor for Firebase.
     */
    public Notification(){
    }

    /**
     * Gets the unique identifier of the notification.
     * @return The notification's UUID.
     */
    public String getNotificationID() {
        return notificationID;
    }

    /**
     * Sets the unique identifier of the notification.
     * @param notificationID The UUID to set.
     */
    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    /**
     * Gets the ID of the recipient user.
     * @return The recipient's UUID.
     */
    public String getRecipientID() {
        return recipientID;
    }

    /**
     * Sets the ID of the recipient user.
     * @param recipientID The recipient's UUID to set.
     */
    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    /**
     * Gets the ID of the related event.
     * @return The event's UUID.
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the ID of the related event.
     * @param eventID The event's UUID to set.
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Gets the notification type.
     * @return The notification type string.
     */
    public String getNotifType() {
        return notifType;
    }

    /**
     * Sets the notification type.
     * @param notifType The notification type string to set.
     */
    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }

    /**
     * Gets the notification's payload (content).
     * @return The payload string.
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the notification's payload (content).
     * @param payload The payload string to set.
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * Gets the timestamp of when the notification was created/sent.
     * @return The sent timestamp as an Timestamp.
     */
    public Timestamp getSentAt() {
        return sentAt;
    }

    /**
     * Sets the timestamp of when the notification was sent.
     * @param sentAt The sent timestamp to set.
     */
    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Checks the delivery status of the notification.
     * @return true if the notification has been successfully delivered, false otherwise.
     */
    public boolean isDeliverStatus() {
        return deliverStatus;
    }

    /**
     * Sets the delivery status of the notification.
     * @param deliverStatus The delivery status to set.
     */
    public void setDeliverStatus(boolean deliverStatus) {
        this.deliverStatus = deliverStatus;
    }

    /**
     * Gets the provider-specific message ID (e.g., from FCM).
     * @return The provider message ID string.
     */
    public String getProviderMsgID() {
        return providerMsgID;
    }

    /**
     * Sets the provider-specific message ID after sending.
     * @param providerMsgID The provider message ID to set.
     */
    public void setProviderMsgID(String providerMsgID) {
        this.providerMsgID = providerMsgID;
    }

    /**
     * Gets the error message if the notification failed to send.
     * @return The error message string.
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message if the notification failed to send.
     * @param error The error message to set.
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Queues the notification to be sent by a background service.
     * (Not yet implemented)
     */
    public void queue(){};

    /**
     * Marks the notification as successfully sent.
     * (Not yet implemented)
     * @param at The exact timestamp when it was confirmed sent.
     * @param providerMsgID The ID from the notification service provider.
     */
    public void markSent(Instant at, String providerMsgID){};

    /**
     * Marks the notification as failed to send.
     * (Not yet implemented)
     * @param at The exact timestamp when the failure was recorded.
     * @param error A description of the error that occurred.
     */
    public void markFailed(Instant at, String error){};

    /**
     * Checks if the notification is in a state where it can be delivered.
     * (Currently a placeholder)
     * @return true if deliverable, false otherwise.
     */
    public boolean isDeliverable(){
        return true; // placeholder
    };

    public boolean isViewedByEntrant() {
        return viewedByEntrant;
    }

    public void setViewedByEntrant(boolean viewedByEntrant) {
        this.viewedByEntrant = viewedByEntrant;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public void setOrganizerID(String organizerID) {
        this.organizerID = organizerID;
    }
}
