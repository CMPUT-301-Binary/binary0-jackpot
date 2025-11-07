package com.example.jackpot;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

public class Image {
    private UUID imageID;
    //private URL url;
    private UUID uploadedBy;

    public Image(UUID uploadedBy) {
        this.imageID = UUID.randomUUID();
        this.uploadedBy = uploadedBy;
    }

    public UUID getImageID() {
        return imageID;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public void setImageID(UUID imageID) {
        this.imageID = imageID;
    }

    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void deleteImage(UUID imageID) {
        if (imageID == this.imageID) {
            this.imageID = null;
            this.uploadedBy = null;
        }
    }

}
