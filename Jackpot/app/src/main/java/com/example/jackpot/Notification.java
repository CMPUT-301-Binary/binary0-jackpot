package com.example.jackpot;

import java.time.Instant;
import java.util.UUID;

public class Notification {
    private UUID notificationID;
    private UUID recipientID;
    private UUID eventID;
    //    private String notificationType;
    private String payload;
    private Instant sentAt;
    private boolean deliverStatus;
    private String providerMsgID;
    private String error;


    public UUID getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(UUID notificationID) {
        this.notificationID = notificationID;
    }

    public UUID getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(UUID recipientID) {
        this.recipientID = recipientID;
    }

    public UUID getEventID() {
        return eventID;
    }

    public void setEventID(UUID eventID) {
        this.eventID = eventID;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isDeliverStatus() {
        return deliverStatus;
    }

    public void setDeliverStatus(boolean deliverStatus) {
        this.deliverStatus = deliverStatus;
    }

    public String getProviderMsgID() {
        return providerMsgID;
    }

    public void setProviderMsgID(String providerMsgID) {
        this.providerMsgID = providerMsgID;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void queue(){};

    public void markSent(Instant at, String providerMsgID){};

    public void markFailed(Instant at, String error){};

    public boolean isDeliverable(){
        return true; // placeholder
    };

}
