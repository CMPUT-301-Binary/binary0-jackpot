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
        event = new Event("event_id", "organizer_id", "Test Event", "Description", 
                        waitingList, new UserList(), new UserList(), "Location", new Date(), 0.0, 0.0, 0.0, 20,
                        new Date(), new Date(), "", "", false, "Category");
    }

    @Test
    public void testDrawEvent_SuccessfulDraw() {
        // Action: Draw 2 entrants from the waiting list of 5
        ArrayList<User> winners = event.drawEvent(2);

        // Verification
        // 1. Check that exactly 2 winners were returned
        assertEquals("Should return the specified number of winners.", 2, winners.size());

        // 2. Check that the original waiting list size has been reduced by 2
        assertEquals("Waiting list should be smaller after the draw.", 3, event.getWaitingList().size());

        // 3. Verify that the winners are no longer in the waiting list
        for (User winner : winners) {
            assertFalse("Drawn winner should not be in the waiting list anymore.", event.getWaitingList().contains(winner));
        }
    }

    @Test
    public void testDrawEvent_DrawTooMany_ThrowsException() {
        // Action & Verification: Attempt to draw 6 entrants from a list of 5
        // This should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            event.drawEvent(6);
        });
    }

    @Test
    public void testDrawEvent_DrawAllEntrants() {
        // Action: Draw all 5 entrants
        ArrayList<User> winners = event.drawEvent(5);

        // Verification
        // 1. Check that 5 winners were returned
        assertEquals("Should return all entrants when drawing the full size.", 5, winners.size());

        // 2. Check that the waiting list is now empty
        assertTrue("Waiting list should be empty after drawing all entrants.", event.getWaitingList().isEmpty());
    }

    @Test
    public void testDrawEvent_DrawZeroEntrants() {
        // Action: Draw 0 entrants
        ArrayList<User> winners = event.drawEvent(0);

        // Verification
        // 1. Check that the returned list is empty
        assertTrue("Drawing zero entrants should return an empty list.", winners.isEmpty());

        // 2. Check that the waiting list size is unchanged
        assertEquals("Waiting list size should not change when drawing zero entrants.", 5, event.getWaitingList().size());
    }
}
