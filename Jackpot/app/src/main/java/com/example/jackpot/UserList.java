package com.example.jackpot;

import java.util.ArrayList;

public class UserList {
    private ArrayList<User> users;
    private Integer capacity;
    public UserList() {
        users = new ArrayList<>();
    }
    public UserList(Integer cap) {
        users = new ArrayList<>();
        capacity = cap;
    }
    public void add(User e) {
        if (users.size() >= capacity) {
            throw new IllegalStateException("User list is full");
        }
        users.add(e);
    }
    public void remove(User e) {
        users.remove(e);
    }
    public ArrayList<User> getUsers() {
        return users;
    }
    public int size() {
        return users.size();
    }
    public User get(int i) {
        return users.get(i);
    }
    public void clear() {
        users.clear();
    }
    public boolean contains(User e) {
        return users.contains(e);
    }
    public void setCapacity(int cap) {
        capacity = cap;
    }
    public Integer getCapacity() {
        return capacity;
    }
}
