package com.example.jackpot;

import org.junit.Test;

import static org.junit.Assert.*;

import java.time.Instant;
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
        Entrant entrant = new Entrant("John Doe", id.toString(), User.Role.ENTRANT,
                "", "", "", "", new Device());
        EntrantList waitingList = new EntrantList(5);
        Event event = new Event(UUID.randomUUID(), "Test", "", waitingList,
                "", 0.0, 0.0, 5.5, 5, Instant.now(), Instant.now(),
                new Image(UUID.randomUUID()), UUID.randomUUID(), true);

        EntrantList waitingList2 = new EntrantList();
        Event event2 = new Event(UUID.randomUUID(), "Test", "", waitingList2,
                "", 0.0, 0.0, 5.5, 5, Instant.now(), Instant.now(),
                new Image(UUID.randomUUID()), UUID.randomUUID(), true);

        EntrantList waitingList3 = new EntrantList(0);
        Event event3 = new Event(UUID.randomUUID(), "Test", "", waitingList3,
                "", 0.0, 0.0, 5.5, 5, Instant.now(), Instant.now(),
                new Image(UUID.randomUUID()), UUID.randomUUID(), true);

        entrant.joinWaitingList(event);
        assertTrue(waitingList.contains(entrant));
        entrant.joinWaitingList(event2);
        assertTrue(waitingList2.contains(entrant));
        assertThrows(IllegalStateException.class, () -> entrant.joinWaitingList(event3));
        assertFalse(waitingList3.contains(entrant));
    }
    @Test
    public void testLeaveWaitingList(){
        UUID id = UUID.randomUUID();
        Entrant entrant = new Entrant("John Doe", id.toString(), User.Role.ENTRANT,
                "", "", "", "", new Device());
        EntrantList waitingList = new EntrantList(5);
        Event event = new Event(UUID.randomUUID(), "Test", "", waitingList,
                "", 0.0, 0.0, 5.5, 5, Instant.now(), Instant.now(),
                new Image(UUID.randomUUID()), UUID.randomUUID(), true);
        waitingList.add(entrant);
        entrant.leaveWaitingList(event);
        assertFalse(waitingList.contains(entrant));
        assertThrows(IllegalArgumentException.class , () -> entrant.leaveWaitingList(event));
    }
}