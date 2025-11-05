package com.example.jackpot;

import java.util.ArrayList;

public class EventList {
    private ArrayList<Event> events;
    public EventList(ArrayList<Event> events) {
        this.events = events;
    }
    public void addEvent(Event event) {
        events.add(event);
    }
    public void removeEvent(Event event) {
        events.remove(event);
    }
    public ArrayList<Event> getEvents() {
        return events;
    }
    public int countEvents() {
        return events.size();
    }
    public Event getEvent(int index) {
        return events.get(index);
    }
    public boolean containsEvent(Event event) {
        return events.contains(event);
    }
    public void clearEvents() {
        events.clear();
    }
    public int indexOfEvent(Event event) {
        return events.indexOf(event);
    }
    public int lastIndexOfEvent(Event event) {
        return events.lastIndexOf(event);
    }
    // sorting method if we must sort events
//    public void sortEvents() {
//        events.sort(null);
//    }
}
