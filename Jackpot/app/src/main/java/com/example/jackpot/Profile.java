package com.example.jackpot;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

public class Profile {
    // Fields
    private UUID profileId;
    private UUID userId;
    private String name;
    private URL avatarUrl;
    private Instant createdAt;

    // Constructors
    public Profile() { }

    public Profile(UUID profileId, UUID userId, String name, URL avatarUrl,
                   Instant createdAt) {
        this.profileId = profileId;
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    // Getters / Setters
    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID id) { this.profileId = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID id) { this.userId = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public URL getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(URL url) { this.avatarUrl = url; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant ts) { this.createdAt = ts; }


    public void updateAvatar(URL url) {
        this.avatarUrl = url;
    }

    public void delete() {

    }

}
