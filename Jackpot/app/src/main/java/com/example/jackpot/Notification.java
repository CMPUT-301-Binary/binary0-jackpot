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

}
