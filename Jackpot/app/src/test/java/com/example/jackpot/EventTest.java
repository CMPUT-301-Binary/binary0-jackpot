package com.example.jackpot;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Unit tests for Event domain logic (draws, lists, counters).
 */
public class EventTest {

    private Event event;
    private UserList waitingList;
    private UserList invitedList;
    private UserList joinedList;
    private UserList cancelledList;

    @Before
    public void setUp() {
        waitingList = new UserList(10);
        invitedList = new UserList();
        joinedList = new UserList();
        cancelledList = new UserList();

        for (int i = 0; i < 5; i++) {
            waitingList.add(fakeEntrant("entrant-" + i));
        }

        event = new Event(
                "event_id",
                "organizer_id",
                "Test Event",
                "Description",
                "Criteria",
                waitingList,
                joinedList,
                invitedList,
                cancelledList,
                "Location",
                new Date(),
                0.0,
                0.0,
                0.0,
                10,
                new Date(),
                new Date(),
                "poster",
                "qr",
                false,
                "Category"
        );
    }

    private Entrant fakeEntrant(String id) {
        return new Entrant(id, "Name " + id, User.Role.ENTRANT, id + "@test.com", "", "", "", "", null, null);
    }

    @Test
    public void drawEvent_waitingListLargerThanCapacity() {
        event.setCapacity(2);

        ArrayList<User> winners = event.drawEvent();

        assertEquals(2, winners.size());
        assertEquals(3, event.getWaitingList().size());
        for (User winner : winners) {
            assertFalse(event.getWaitingList().contains(winner));
            assertTrue(event.getInvitedList().contains(winner));
        }
    }

    @Test
    public void drawEvent_waitingListSmallerThanCapacity() {
        event.setCapacity(10);

        ArrayList<User> winners = event.drawEvent();

        assertEquals(5, winners.size());
        assertTrue(event.getWaitingList().isEmpty());
        assertEquals(5, event.getInvitedList().size());
    }

    @Test
    public void drawEvent_zeroCapacityReturnsEmpty() {
        event.setCapacity(0);
        ArrayList<User> winners = event.drawEvent();
        assertTrue(winners.isEmpty());
        assertEquals(5, event.getWaitingList().size());
    }

    @Test(expected = NullPointerException.class)
    public void drawEvent_nullWaitingListThrows() {
        event.setWaitingList(null);
        event.drawEvent();
    }

    @Test
    public void drawEvent_emptyWaitingListReturnsEmpty() {
        event.setWaitingList(new UserList(5));
        ArrayList<User> winners = event.drawEvent();
        assertTrue(winners.isEmpty());
        assertEquals(0, event.getInvitedList().size());
    }

    @Test
    public void addEntrantWaitingList_removesCancelledDuplicate() {
        Entrant entrant = fakeEntrant("dup");
        cancelledList.add(entrant);
        event.addEntrantWaitingList(entrant);
        assertTrue(waitingList.contains(entrant));
        assertFalse(cancelledList.contains(entrant));
    }

    @Test
    public void hasEntrantDetectsAcrossAllLists() {
        Entrant waiting = fakeEntrant("w");
        Entrant invited = fakeEntrant("i");
        Entrant joined = fakeEntrant("j");
        Entrant cancelled = fakeEntrant("c");

        waitingList.add(waiting);
        invitedList.add(invited);
        joinedList.add(joined);
        cancelledList.add(cancelled);

        assertTrue(event.hasEntrant("w"));
        assertTrue(event.hasEntrant("i"));
        assertTrue(event.hasEntrant("j"));
        assertTrue(event.hasEntrant("c"));
        assertFalse(event.hasEntrant("missing"));
    }

    @Test
    public void joinFromOtherListsBlockedByHasEntrant() {
        Entrant invited = fakeEntrant("block");
        invitedList.add(invited);
        assertTrue(event.hasEntrant("block"));
        assertThrows(IllegalArgumentException.class, () -> invited.joinWaitingList(event));
    }

    @Test
    public void cancelledEntrantCanRejoinWaitingList() {
        Entrant cancelled = fakeEntrant("cancel-rejoin");
        cancelledList.add(cancelled);
        // Previously hasEntrant would have blocked; now cancelled can rejoin
        cancelled.joinWaitingList(event);
        assertTrue(waitingList.contains(cancelled));
        assertFalse(cancelledList.contains(cancelled));
    }

    @Test(expected = IllegalStateException.class)
    public void addEntrantWaitingList_respectsCapacity() {
        event.setWaitingList(new UserList(1));
        event.addEntrantWaitingList(fakeEntrant("one"));
        event.addEntrantWaitingList(fakeEntrant("two"));
    }

    @Test
    public void removeEntrantWaitingList_removesOnlyOnce() {
        Entrant entrant = fakeEntrant("rem");
        event.addEntrantWaitingList(entrant);
        event.removeEntrantWaitingList(entrant);
        assertFalse(event.getWaitingList().contains(entrant));
    }

    @Test
    public void entrantInList_handlesNulls() {
        assertFalse(event.entrantInList("x", null));
        assertFalse(event.entrantInList(null, waitingList));
    }

    @Test
    public void invitedWaitingCountsReflectLists() {
        assertEquals(5, event.getWaitingCount());
        assertEquals(0, event.getInvitedCount());
        invitedList.add(fakeEntrant("invited"));
        assertEquals(1, event.getInvitedCount());
    }
}
