package com.example.jackpot;

import static org.junit.Assert.*;

import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * Unit tests for entrant-centric flows (join/leave/cancel lists).
 */
public class EntrantTest {

    private Entrant entrant;
    private Event baseEvent;
    private UserList waitingList;
    private UserList invitedList;
    private UserList joinedList;
    private UserList cancelledList;

    @Before
    public void setUp() {
        entrant = new Entrant(
                UUID.randomUUID().toString(),
                "Test Entrant",
                User.Role.ENTRANT,
                "test@test.com",
                "555-5555",
                "",
                "",
                "",
                new Device(),
                new GeoPoint(53, -113)
        );

        waitingList = new UserList(2);
        invitedList = new UserList();
        joinedList = new UserList();
        cancelledList = new UserList();

        baseEvent = new Event(
                UUID.randomUUID().toString(),
                "org-1",
                "Test Event",
                "Desc",
                "Criteria",
                waitingList,
                joinedList,
                invitedList,
                cancelledList,
                "Location",
                new Date(),
                0.0,
                0.0,
                10.0,
                2,
                new Date(),
                new Date(),
                "poster",
                "qr",
                false,
                "Category"
        );
    }

    @Test
    public void joinWaitingList_addsEntrant() {
        entrant.joinWaitingList(baseEvent);
        assertTrue(waitingList.contains(entrant));
    }

    @Test
    public void joinWaitingList_preventsDuplicates() {
        entrant.joinWaitingList(baseEvent);
        assertThrows(IllegalArgumentException.class, () -> entrant.joinWaitingList(baseEvent));
    }

    @Test
    public void joinWaitingList_honorsCapacity() {
        Entrant other = new Entrant(UUID.randomUUID().toString(), "Other", User.Role.ENTRANT, "", "", "", "", "", new Device(), new GeoPoint(0, 0));
        entrant.joinWaitingList(baseEvent);
        other.joinWaitingList(baseEvent);
        Entrant third = new Entrant(UUID.randomUUID().toString(), "Third", User.Role.ENTRANT, "", "", "", "", "", new Device(), new GeoPoint(0, 0));
        assertThrows(IllegalStateException.class, () -> third.joinWaitingList(baseEvent));
    }

    @Test
    public void leaveWaitingList_removesEntrant() {
        entrant.joinWaitingList(baseEvent);
        entrant.leaveWaitingList(baseEvent);
        assertFalse(waitingList.contains(entrant));
    }

    @Test
    public void leaveWaitingList_throwsWhenNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> entrant.leaveWaitingList(baseEvent));
    }

    @Test
    public void rejoinMovesFromCancelledToWaiting() {
        entrant.joinWaitingList(baseEvent);
        waitingList.remove(entrant);
        cancelledList.add(entrant);

        entrant.joinWaitingList(baseEvent);

        assertTrue(waitingList.contains(entrant));
        assertFalse(cancelledList.contains(entrant));
    }
}
