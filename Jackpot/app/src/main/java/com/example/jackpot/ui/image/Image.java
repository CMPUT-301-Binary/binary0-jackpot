package com.example.jackpot.ui.image;

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
 * Non-Responsibilities: no image decoding, rendering, or I/O.
 */
public class Image {

    // Image types - only POSTER and QR_CODE are allowed
    public static final String TYPE_POSTER = "POSTER";
    public static final String TYPE_QR_CODE = "QR_CODE";

    // Display order constants
    public static final int ORDER_POSTER = 0;
    public static final int ORDER_QR_CODE = 1;

    private String imageID;
    private String uploadedBy;
    private String imageUrl;
    private String imageType;
    private int displayOrder;



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
     * @param imageType  The type of the image (e.g., "poster" or "QR_code").
     * @param displayOrder The order in which the image should be displayed.
     */
    public Image(String imageID, String uploadedBy, String imageUrl, String imageType, int displayOrder) {
        this.imageID = imageID;
        this.uploadedBy = uploadedBy;
        this.imageUrl = imageUrl;
        this.imageType = imageType; //poster or QR_code, (later add profile)
        this.displayOrder = displayOrder; // 0 = poster, 1 = QR_code
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

    /**
     * Gets the type of the image.
     *
     * @return A string representing the type of the image (poster or QR_code).
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * Sets the type of the image.
     *
     * @param imageType A string representing the type to set (poster or QR_code).
     */
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * Gets the display order of the image.
     *
     * @return An integer representing the display order of the image.
    */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the display order of the image.
     *
     * @param displayOrder An integer representing the display order to set.
    */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Check if this is the poster image.
     */
    public boolean isPoster() {
        return TYPE_POSTER.equals(imageType) && displayOrder == ORDER_POSTER;
    }

    /**
     * Check if this is a QR code image.
     */
    public boolean isQrCode() {
        return TYPE_QR_CODE.equals(imageType) && displayOrder == ORDER_QR_CODE;
    }

    /**
     * Validates that the image type is one of the allowed types.
     */
    public boolean isValidType() {
        return TYPE_POSTER.equals(imageType) || TYPE_QR_CODE.equals(imageType);
    }
}
