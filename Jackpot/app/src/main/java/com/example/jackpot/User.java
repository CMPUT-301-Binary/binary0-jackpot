package com.example.jackpot;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Objects;



/**
 * Represents a user of the Jackpot application.
 * This class models the data for a user, including their personal information,
 * role within the app, and device details. It is Serializable to allow
 * it to be passed between Android components, such as activities and fragments.
 */
public class User implements Serializable {
    protected String id;  // Changed from UUID to String
    protected String name;
    protected Role role;
    protected String email;
    protected String phone; //Could be NULL
    protected String profileImageUrl; // New field for Firebase image URL
    protected String password;
    protected String notificationPreferences;
    protected Device device;
    protected GeoPoint geoPoint;


    /**
     * A public, no-argument constructor.
     * This is required by Firebase Firestore for automatic data deserialization
     * from a Firestore document into a User object.
     */
    public User() {
        // Firestore will populate fields using setters
    }

    /**
     * Constructs a new User with all its properties.
     *
     * @param id The unique identifier for the user (e.g., from Firebase Auth).
     * @param name The full name of the user.
     * @param role The user's role within the application (e.g., ENTRANT, ORGANIZER).
     * @param email The user's email address.
     * @param phone The user's phone number (can be null).
     * @param profileImageUrl The URL of the user's profile image.
     * @param password The user's password.
     * @param notificationPreferences User's preferences for notifications.
     * @param device The device associated with the user for notifications.
     * @param geoPoint The location of the user on the map.
     */

    public User(String id, String name, Role role, String email, String phone, String profileImageUrl, String password, String notificationPreferences, Device device, GeoPoint geoPoint) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.password = password;
        this.notificationPreferences = notificationPreferences;
        this.device = device;
        this.geoPoint = geoPoint;
    }
    /**
     * Gets the unique identifier of the user.
     * @return The user's ID string.
     */

    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the user.
     * @param id The new ID for the user.
     */

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the user.
     * @return The user's name.
     */

    public String getName() {
        return name;
    }

    /**
     * Sets the name for the user.
     * @param name The new name for the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the role of the user within the app.
     * @return The user's {@link Role}.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the role for the user.
     * @param role The new {@link Role} for the user.
     */

    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets the email address of the user.
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for the user.
     * @param email The new email for the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the user.
     * @return The user's phone number, which may be null.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number for the user.
     * @param phone The new phone number for the user.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the URL of the user's profile image.
     * @return The URL of the user's profile image.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the URL of the user's profile image.
     * @param profileImageUrl The new URL for the user's profile image.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the password of the user.
     * Note: This is hidden in firebase. It must be at least 6 characters long.
     * @return The user's password string.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the user.
     * @param password The new password for the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's notification preferences.
     * @return A string representing notification settings.
     */
    public String getNotificationPreferences() {
        return notificationPreferences;
    }

    /**
     * Sets the user's notification preferences.
     * @param notificationPreferences The new notification settings string.
     */
    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }

    /**
     * Gets the device associated with the user.
     * @return The user's {@link Device} object.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the device for the user.
     * @param device The new {@link Device} object for the user.
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    public enum Role {
        ENTRANT,
        ORGANIZER,
        ADMIN
    }

    /**
     * get location of user.
     * @return stored {@link GeoPoint} or null if unset.
     */
    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    /**
     * set location of user.
     * @param geoPoint The new {@link GeoPoint} object for the user.
     */
    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
