package com.example.jackpot;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.jackpot.ui.image.Image;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.UUID;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class EntrantTest {

    @Test
    public void testJoinWaitingList() {
        UUID id = UUID.randomUUID();
        Entrant entrant1 = new Entrant(
                "John Doe",
                id.toString(),
                User.Role.ENTRANT,
                "", "", "", "", "", new Device(), new GeoPoint(0,0)
        );
        Entrant entrant2 = new Entrant(
                "John Day",
                UUID.randomUUID().toString(),
                User.Role.ENTRANT,
                "", "", "", "", "", new Device(), new GeoPoint(0,0)
        );

        UserList waitingList = new UserList(5);
        Event event = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList,
                "",
                new Date(),
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                "",
                "",
                true,
                ""
        );

        UserList waitingList2 = new UserList();
        Event event2 = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList2,
                "",
                new Date(),
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                "",
                "",
                true,
                ""
        );

        UserList waitingList3 = new UserList(1);
        Event event3 = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList3,
                "",
                new Date(),
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                "",
                "",
                true,
                ""
        );

        entrant1.joinWaitingList(event);
        assertTrue(waitingList.contains(entrant1));

        entrant1.joinWaitingList(event2);
        assertTrue(waitingList2.contains(entrant1));

        entrant1.joinWaitingList(event3);
        assertTrue(waitingList3.contains(entrant1));
        assertThrows(IllegalStateException.class, () -> entrant2.joinWaitingList(event3));
        assertFalse(waitingList3.contains(entrant2));
    }

    @Test
    public void testLeaveWaitingList() {
        UUID id = UUID.randomUUID();
        Entrant entrant = new Entrant(
                "John Doe",
                id.toString(),
                User.Role.ENTRANT,
                "", "", "", "", "", new Device(), new GeoPoint(0,0)
        );

        UserList waitingList = new UserList(5);
        Event event = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList,
                "",
                new Date(),
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                "",
                "",
                true,
                ""
        );

        waitingList.add(entrant);
        entrant.leaveWaitingList(event);
        assertFalse(waitingList.contains(entrant));

        assertThrows(IllegalArgumentException.class, () -> entrant.leaveWaitingList(event));
    }
}

