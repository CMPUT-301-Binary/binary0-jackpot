package com.example.jackpot;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.UUID;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

public class EntrantTest {

    @Test
    public void testJoinWaitingList() {
        UUID id = UUID.randomUUID();
        Entrant entrant = new Entrant(
                "John Doe",
                id.toString(),
                User.Role.ENTRANT,
                "", "", "", "", new Device()
        );

        EntrantList waitingList = new EntrantList(5);
        Event event = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList,
                "",
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                new Image(
                        UUID.randomUUID().toString(),
                        "gs://dummy-link/test.jpg",
                        "tester@example.com"
                ),
                UUID.randomUUID().toString(),
                true
        );

        EntrantList waitingList2 = new EntrantList();
        Event event2 = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList2,
                "",
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                new Image(
                        UUID.randomUUID().toString(),
                        "gs://dummy-link/test2.jpg",
                        "tester@example.com"
                ),
                UUID.randomUUID().toString(),
                true
        );

        EntrantList waitingList3 = new EntrantList(0);
        Event event3 = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList3,
                "",
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                new Image(
                        UUID.randomUUID().toString(),
                        "gs://dummy-link/test3.jpg",
                        "tester@example.com"
                ),
                UUID.randomUUID().toString(),
                true
        );

        entrant.joinWaitingList(event);
        assertTrue(waitingList.contains(entrant));

        entrant.joinWaitingList(event2);
        assertTrue(waitingList2.contains(entrant));

        assertThrows(IllegalStateException.class, () -> entrant.joinWaitingList(event3));
        assertFalse(waitingList3.contains(entrant));
    }

    @Test
    public void testLeaveWaitingList() {
        UUID id = UUID.randomUUID();
        Entrant entrant = new Entrant(
                "John Doe",
                id.toString(),
                User.Role.ENTRANT,
                "", "", "", "", new Device()
        );

        EntrantList waitingList = new EntrantList(5);
        Event event = new Event(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Test",
                "",
                waitingList,
                "",
                0.0,
                0.0,
                5.5,
                5,
                new Date(),
                new Date(),
                new Image(
                        UUID.randomUUID().toString(),
                        "gs://dummy-link/test4.jpg",
                        "tester@example.com"
                ),
                UUID.randomUUID().toString(),
                true
        );

        waitingList.add(entrant);
        entrant.leaveWaitingList(event);
        assertFalse(waitingList.contains(entrant));

        assertThrows(IllegalArgumentException.class, () -> entrant.leaveWaitingList(event));
    }
}
