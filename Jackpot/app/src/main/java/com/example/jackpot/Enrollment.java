package com.example.jackpot;

import java.util.UUID;

public class Enrollment {
    private int enrollmentId;
    private UUID userID;
    private UUID eventID;
    private String enrollmentStatus;

    public Enrollment(int enrollmentId, UUID userID, UUID eventID, String enrollmentDate) {
        this.enrollmentId = enrollmentId;
        this.userID = userID;
        this.eventID = eventID;
    }

    public int getEnrollmentId() {
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

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public void setEventID(UUID eventID) {
        this.eventID = eventID;
    }
}
