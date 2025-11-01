package com.example.jackpot;

import java.util.ArrayList;

public class EntrantList {
    private ArrayList<Entrant> entrants;
    public EntrantList() {
        entrants = new ArrayList<Entrant>();;
    }
    public void add(Entrant e) {
        entrants.add(e);
    }
    public void remove(Entrant e) {
        entrants.remove(e);
    }
    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }
    public int size() {
        return entrants.size();
    }
    public Entrant get(int i) {
        return entrants.get(i);
    }
    public void clear() {
        entrants.clear();
    }
    public boolean contains(Entrant e) {
        return entrants.contains(e);
    }
}
