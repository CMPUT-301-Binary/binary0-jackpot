package com.example.jackpot;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;

public class EventTest {

    private Event event;
    private UserList waitingList;

    @Before
    public void setUp() {
        // Create a UserList to act as the waiting list
        waitingList = new UserList(10);
        
        // Add 5 mock entrants to the waiting list before each test
        for (int i = 0; i < 5; i++) {
            Entrant entrant = new Entrant("entrant_id_" + i, "Entrant " + i, User.Role.ENTRANT, "e"+i+"@test.com", "", "", "", "", null, null);
            waitingList.add(entrant);
        }

        // Create a new event instance with the pre-populated waiting list
        // Capacity is initially set to a value larger than waiting list
        event = new Event("event_id", "organizer_id", "Test Event", "Description", 
                        waitingList, new UserList(), new UserList(), "Location", new Date(), 0.0, 0.0, 0.0, 10,
                        new Date(), new Date(), "", "", false, "Category");
    }

    @Test
    public void testDrawEvent_WaitingListLargerThanCapacity() {
        // Set capacity to be smaller than the waiting list
        event.setCapacity(2);
        
        // Action: Draw entrants. Should draw 'capacity' number of winners.
        ArrayList<User> winners = event.drawEvent();

        // Verification
        // 1. Check that 'capacity' winners were returned
        assertEquals("Should return the specified number of winners based on capacity.", 2, winners.size());

        // 2. Check that the original waiting list size has been reduced by 'capacity'
        assertEquals("Waiting list should be smaller after the draw.", 3, event.getWaitingList().size());

        // 3. Verify that the winners are no longer in the waiting list
        for (User winner : winners) {
            assertFalse("Drawn winner should not be in the waiting list anymore.", event.getWaitingList().contains(winner));
        }
        
        // 4. Verify winners are in the invited list
        for (User winner : winners) {
            assertTrue("Drawn winner should be in the invited list.", event.getInvitedList().contains(winner));
        }
    }

    @Test
    public void testDrawEvent_WaitingListSmallerThanCapacity() {
        // Capacity (10) is larger than waiting list (5) by default from setUp
        
        // Action: Draw all entrants
        ArrayList<User> winners = event.drawEvent();

        // Verification
        // 1. Check that all 5 winners were returned
        assertEquals("Should return all entrants when waiting list is smaller than capacity.", 5, winners.size());

        // 2. Check that the waiting list is now empty
        assertTrue("Waiting list should be empty after drawing all entrants.", event.getWaitingList().isEmpty());
        
        // 3. Check that all original entrants are now winners
        assertEquals("All original waiting list members should be winners.", 5, event.getInvitedList().size());
    }

    @Test
    public void testDrawEvent_ZeroCapacity() {
        // Set capacity to 0
        event.setCapacity(0);
        
        // Action: Draw 0 entrants
        ArrayList<User> winners = event.drawEvent();

        // Verification
        // 1. Check that the returned list is empty
        assertTrue("Drawing with zero capacity should return an empty list.", winners.isEmpty());

        // 2. Check that the waiting list size is unchanged
        assertEquals("Waiting list size should not change when capacity is zero.", 5, event.getWaitingList().size());
    }

    @Test
    public void testDrawEvent_NullWaitingList_ThrowsException() {
        event.setWaitingList(null);
        assertThrows(NullPointerException.class, () -> {
            event.drawEvent();
        });
    }
    
    @Test
    public void testDrawEvent_EmptyWaitingList() {
        event.setWaitingList(new UserList(10));
        
        ArrayList<User> winners = event.drawEvent();
        
        assertTrue("Should return empty list when waiting list is empty", winners.isEmpty());
        assertEquals("Invited list should be empty", 0, event.getInvitedList().size());
    }
}
