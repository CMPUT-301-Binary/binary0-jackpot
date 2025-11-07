package com.example.jackpot;

import java.net.URL;
import java.util.*;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: QRCode.java
 *
 * Purpose/Role:
 *  Model for a QR code used in event discovery/check-in flows.
 *  Encapsulates the payload, optional metadata, and simple validation.
 *  Pure data: no UI, no Android context.
 *
 * Design Notes:
 *  - Belongs to the Model layer (MVVM/MVC).
 *  - Payload must not contain secrets, treat the code as an opaque identifier only.
 */

/**
 * Represents a QR code, containing its unique identifier and URL.
 * This class is a data model and does not have any UI or Android-specific dependencies.
 */
public class QRCode {
    private UUID qrCodeID;

    private URL qrCodeURL;

    private String eventName;

    /**
     * Creates a QRCode model.
     *
     * @param qrCodeID the unique identifier of this QR code.
     * @param qrCodeURL the URL of the QR code.
     */
    public QRCode(UUID qrCodeID, URL qrCodeURL) {
        this.qrCodeID = qrCodeID;
        this.qrCodeURL = qrCodeURL;
    }

    /** @return the unique identifier of this QR code. */
    public UUID getQRCodeID() {
        return qrCodeID;
    }

    /**
     * @return the URL of the QR code.
     */
    public URL getQRCodeURL() {
        return qrCodeURL;
    }

    /**
     * @return the name of the event.
     */
    public String getEventName() {
        return eventName;
    }

//    /**
//     * Sets the unique identifier of this QR code.
//     */
//    public void setQrCodeID(){
//        this.qrCodeID = qrCodeID;
//    }

    /**
     * Sets the URL of the QR code.
     * @param qrCodeURL the URL of the QR code.
     */
    public void setQRCodeURL(URL qrCodeURL) {
        this.qrCodeURL = qrCodeURL;
    }

    /**
     * Sets the name of the event.
     * @param eventName the name of the event.
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }



}
