package com.example.jackpot;

import java.util.ArrayList;

/*
 * CMPUT 301 – Event Lottery App (“Jackpot”)
 * File: EventList.java
 *
 * Purpose/Role:
 *   Lightweight collection wrapper for {@code Event} objects used by UI/ViewModel code.
 *   Centralizes common list operations (add/remove/find/count) and keeps list mutation in one place.
 *
 * Design Notes:
 *   - Simple façade over {@link java.util.ArrayList}; not thread-safe.
 *   - Caller owns ordering policy. Sorting is currently external.
 *   - Prefer returning unmodifiable views if exposing this list to UI adapters.
 *
 * Outstanding Issues / TODOs:
 *   - TODO: Add optional sort/filter helpers.
 *   - TODO: Add null-safety checks on inputs and constructor list.
 */

/**
 * Mutable container for {@link Event} items with convenience methods for common operations.
 */
public class EventList {
    private ArrayList<Event> events;

    /**
     * Default constructor.
     */
    public EventList() {
        events = new ArrayList<>();
    }

    /**
     * Constructor.
     * @param events list of events
     */
    public EventList(ArrayList<Event> events) {
        this.events = events;
    }

    /**
     * Add an event to the list.
     * @param event event to add
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    /**
     * Remove an event from the list.
     * @param event event to remove
     */
    public void removeEvent(Event event) {
        events.remove(event);
    }

    /**
     * Get the list of events.
     * @return list of events
     */
    public ArrayList<Event> getEvents() {
        return events;
    }

    /**
     * Set the list of events.
     * @param events list of events
     */
    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    /**
     * Get the number of events in the list.
     * @return number of events in the list
     */
    public int countEvents() {
        return events.size();
    }

    /**
     * Get an event from the list.
     * @param index index of event
     * @return event at index
     */
    public Event getEvent(int index) {
        return events.get(index);
    }

    /**
     * Check if an event is in the list.
     * @param event event to check
     * @return true if event is in the list
     */
    public boolean containsEvent(Event event) {
        return events.contains(event);
    }

    /**
     * Clear the list of events.
     */
    public void clearEvents() {
        events.clear();
    }

    /**
     * Get the index of an event in the list.
     * @param event event to find
     * @return index of first occurrence
     */
    public int indexOfEvent(Event event) {
        return events.indexOf(event);
    }

    /**
     * Get the last index of an event in the list.
     * @param event event to find
     * @return index of last occurrence
     */
    public int lastIndexOfEvent(Event event) {
        return events.lastIndexOf(event);
    }
    // sorting method if we must sort events
//    public void sortEvents() {
//        events.sort(null);
//    }
}
