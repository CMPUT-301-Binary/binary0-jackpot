package com.example.jackpot;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: Image.java
 *
 * Purpose/Role:
 *   Model for image metadata stored/used by the app (e.g., event cover, profile avatar).
 *   Holds IDs, storage path/URL, mime/type, dimensions, and timestamps. Pure data: no UI.
 *
 * Design Notes:
 *   - Model layer (MVVM/MVC). Keep Android-agnostic: no Bitmap/Drawable here.
 *   - Do not store secrets; URLs should be short-lived or storage paths + tokenized fetch.
 *
 * Outstanding Issues / TODOs:
 *   - TODO: Add validation (mime/type whitelist, max dimensions/size).
 */


/**
 * Represents an image within the application, such as an event poster or user profile picture.
 * Contains metadata for an application image (e.g., profile avatar or event banner).
 * Keeps identifiers, source location, and optional presentation hints.
 *
 * Non-Responsibilities: no image decoding, rendering, or I/O.
 */
public class Image {
    private String imageID;
    private String uploadedBy;
    private String imageUrl;

    /**
     * Default constructor required for frameworks like Firebase that use reflection
     * to instantiate objects from data snapshots.
     */
    public Image() {
        // Needed for Firestore
    }

    /**
     * Constructs a new {@code Image} instance.
     *
     * @param imageID    A unique identifier for the image.
     * @param uploadedBy The identifier of the user who uploaded the image.
     * @param imageUrl   The public URL where the image is stored and can be retrieved.
     */
    public Image(String imageID, String uploadedBy, String imageUrl) {
        this.imageID = imageID;
        this.uploadedBy = uploadedBy;
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the unique identifier for this image.
     *
     * @return The image's unique ID.
     */
    public String getImageID() {
        return imageID;
    }

    /**
     * Sets the unique identifier for this image.
     *
     * @param imageID The unique ID to set.
     */
    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    /**
     * Gets the identifier of the user who uploaded the image.
     *
     * @return The uploader's user ID.
     */
    public String getUploadedBy() {
        return uploadedBy;
    }

    /**
     * Sets the identifier of the user who uploaded the image.
     *
     * @param uploadedBy The uploader's user ID to set.
     */
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    /**
     * Gets the public URL of the image.
     *
     * @return A string representing the URL where the image is hosted.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the public URL of the image.
     *
     * @param imageUrl A string representing the URL to set.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
