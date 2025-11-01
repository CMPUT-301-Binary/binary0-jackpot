package com.example.jackpot;

import java.util.UUID;



public abstract class User {
    protected final UUID id;
    protected String name;
    protected Role role;
    protected String email;
    protected String phone; //Could be NULL
    protected String password;
    protected String notificationPreferences;
    protected Device device;

    public User(UUID id, String name, Role role, String email, String phone, String password, String notificationPreferences, Device device) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.notificationPreferences = notificationPreferences;
        this.device = device;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public enum Role {
        ENTRANT,
        ORGANIZER,
        ADMIN
    }
}
