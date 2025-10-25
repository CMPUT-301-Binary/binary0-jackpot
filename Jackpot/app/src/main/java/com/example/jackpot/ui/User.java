package com.example.jackpot.ui;

import java.util.UUID;

public class User {
    private final UUID id;
    private String name;
    private String role;
    private String email;
    private String phone; //Could be NULL
    private String password;
    private String deviceID;

    public User(String name, String role, String email, String phone, String password, String deviceID) {
        this.id = java.util.UUID.randomUUID();
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.deviceID = deviceID;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
