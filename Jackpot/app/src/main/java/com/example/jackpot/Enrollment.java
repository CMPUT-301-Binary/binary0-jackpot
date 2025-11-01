package com.example.jackpot;

import java.util.UUID;

public class Enrollment extends Notification{
    private UUID enrollmentId;
    private UUID userID;
    private UUID eventID;
    private String enrollmentStatus;

    public Enrollment(UUID userID, UUID eventID, String enrollmentDate) {
        super(userID, eventID, enrollmentDate);
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getUserID() {
        return userID;
    }

    public UUID getEventID() {
        return eventID;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public void setEnrollmentId(UUID enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public void setEventID(UUID eventID) {
        this.eventID = eventID;
    }
}
