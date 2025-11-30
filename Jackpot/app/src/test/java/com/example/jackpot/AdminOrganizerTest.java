package com.example.jackpot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;

/**
 * Simple constructor sanity tests for admin and organizer user models.
 */
public class AdminOrganizerTest {

    @Test
    public void organizerConstructor_setsRoleAndFields() {
        GeoPoint geoPoint = new GeoPoint(53, -113);
        Organizer organizer = new Organizer(
                "Org Name",
                "org-1",
                User.Role.ORGANIZER,
                "org@test.com",
                "555-1234",
                "profile",
                "pass",
                "all",
                new Device(),
                geoPoint
        );

        assertEquals(User.Role.ORGANIZER, organizer.getRole());
        assertEquals("Org Name", organizer.getName());
        assertEquals("org@test.com", organizer.getEmail());
        assertEquals(geoPoint, organizer.getGeoPoint());
    }

    @Test
    public void adminConstructor_setsRoleAdmin() {
        GeoPoint geoPoint = new GeoPoint(0, 0);
        Admin admin = new Admin(
                "Admin Name",
                "admin-1",
                User.Role.ADMIN,
                "admin@test.com",
                "555-9999",
                "profile",
                "pass",
                "none",
                new Device(),
                geoPoint
        );

        assertEquals(User.Role.ADMIN, admin.getRole());
        assertEquals("admin-1", admin.getId());
        assertEquals("admin@test.com", admin.getEmail());
        assertNotNull(admin.getGeoPoint());
    }

    @Test
    public void organizerIgnoresPassedRoleAndRemainsOrganizer() {
        Organizer organizer = new Organizer(
                "Any",
                "org-2",
                User.Role.ADMIN, // pass wrong role intentionally
                "org2@test.com",
                "",
                "",
                "",
                "",
                new Device(),
                new GeoPoint(0, 0)
        );
        assertEquals(User.Role.ORGANIZER, organizer.getRole());
    }

    @Test
    public void adminNameDefaultsToTestAdmin() {
        Admin admin = new Admin(
                "Custom Name",
                "admin-2",
                User.Role.ADMIN,
                "admin2@test.com",
                "",
                "",
                "",
                "",
                new Device(),
                new GeoPoint(0, 0)
        );
        assertEquals("TestAdmin", admin.getName());
    }

    @Test
    public void adminProfileImageDefaults() {
        Admin admin = new Admin(
                "Custom Name",
                "admin-3",
                User.Role.ADMIN,
                "admin3@test.com",
                "",
                "customProfile",
                "",
                "",
                new Device(),
                new GeoPoint(0, 0)
        );
        assertEquals("default", admin.getProfileImageUrl());
    }
}
