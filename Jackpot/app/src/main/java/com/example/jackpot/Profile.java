package com.example.jackpot;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: Profile.java
 *
 * Purpose/Role:
 *  User profile model shared across Entrant/Organizer/Admin flows.
 *  Stores identity and lightweight preferences (name, avatar URL, timestamps).
 *  Pure data: no Android context or UI dependencies.
 *
 * Design Notes:
 *  - Lives in the Model layer (MVVM/MVC).
 *  - Do not store secrets or auth tokens here (handled by Firebase Auth).
 *
 * Outstanding Issues / TODOs:
 *   - TODO: Decide required vs optional fields; add validation (non-empty name, etc.).
 */

/**
 * Represents a user profile in the application.
 * This class serves as a data model for user profiles, containing information such as user ID, name, avatar URL, and creation timestamp.
 * It is a plain Java object (POJO) with no Android-specific dependencies, making it suitable for use in the data layer of the application.
 */
public class Profile {
    // Fields
    private UUID profileId;
    private UUID userId;
    private String name;
    private URL avatarUrl;
    private Instant createdAt;

    // Constructors
    public Profile() { }

    /**
     * Constructs a new Profile object.
     * @param profileId The unique identifier for the profile.
     * @param userId The unique identifier for the user.
     * @param name The name of the user.
     * @param avatarUrl The URL of the user's avatar.
     * @param createdAt The timestamp of when the profile was created.
     */
    public Profile(UUID profileId, UUID userId, String name, URL avatarUrl,
                   Instant createdAt) {
        this.profileId = profileId;
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    // Getters / Setters
    /**
     * Gets the profile's unique identifier.
     * @return The profile's UUID.
     */
    public UUID getProfileId() { return profileId; }
    /**
     * Sets the profile's unique identifier.
     * @param id The UUID to set.
     */
    public void setProfileId(UUID id) { this.profileId = id; }

    /**
     * Gets the user's unique identifier.
     * @return The user's UUID.
     */
    public UUID getUserId() { return userId; }
    /**
     * Sets the user's unique identifier.
     * @param id The UUID to set.
     */
    public void setUserId(UUID id) { this.userId = id; }

    /**
     * Gets the user's name.
     * @return The user's name.
     */
    public String getName() { return name; }
    /**
     * Sets the user's name.
     * @param name The name to set.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the URL of the user's avatar.
     * @return The avatar URL.
     */
    public URL getAvatarUrl() { return avatarUrl; }
    /**
     * Sets the URL of the user's avatar.
     * @param url The avatar URL to set.
     */
    public void setAvatarUrl(URL url) { this.avatarUrl = url; }

    /**
     * Gets the timestamp of when the profile was created.
     * @return The creation timestamp.
     */
    public Instant getCreatedAt() { return createdAt; }
    /**
     * Sets the timestamp of when the profile was created.
     * @param ts The creation timestamp to set.
     */
    public void setCreatedAt(Instant ts) { this.createdAt = ts; }


    /**
     * Updates the user's avatar URL.
     * @param url The new avatar URL.
     */
    public void updateAvatar(URL url) {
        this.avatarUrl = url;
    }

//    public void delete() { // likely want to handle on UI interaction
//        // remove from all lists
//        // remove from db
//    }

}
