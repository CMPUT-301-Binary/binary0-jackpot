package com.example.jackpot;

import java.util.UUID;

/**
 * This class will be used to represent a device, and each user has a device.
 * Todo: Determine how to integrate this with our system, if at all.
 */
public class Device {
    private String deviceID;
    public Device() {
        this.deviceID = UUID.randomUUID().toString();
    }
}
