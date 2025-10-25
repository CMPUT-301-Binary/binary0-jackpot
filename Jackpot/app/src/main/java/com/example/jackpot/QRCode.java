package com.example.jackpot;

import java.net.URL;
import java.util.*;

public class QRCode {
    private UUID qrCodeID;

    private URL qrCodeURL;

    private String eventName;

    public QRCode(UUID qrCodeID, URL qrCodeURL) {
        this.qrCodeID = qrCodeID;
        this.qrCodeURL = qrCodeURL;
    }

    public UUID getQRCodeID() {
        return qrCodeID;
    }

    public URL getQRCodeURL() {
        return qrCodeURL;
    }

    public String getEventName() {
        return eventName;
    }

    public void setQrCodeID(){
        this.qrCodeID = qrCodeID;
    }

    public void setQRCodeURL(URL qrCodeURL) {
        this.qrCodeURL = qrCodeURL;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }



}
