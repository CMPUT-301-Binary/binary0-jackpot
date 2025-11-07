package com.example.jackpot;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

public class Image {
    private String imageID;
    private String uploadedBy;
    private String imageUrl;

    public Image() {
        // Needed for Firestore
    }

    public Image(String imageID, String uploadedBy, String imageUrl) {
        this.imageID = imageID;
        this.uploadedBy = uploadedBy;
        this.imageUrl = imageUrl;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

