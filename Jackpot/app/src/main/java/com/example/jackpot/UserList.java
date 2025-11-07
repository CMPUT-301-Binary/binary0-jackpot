package com.example.jackpot;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * UserList class that holds a list of users with an optional capacity limit.
 * Implements Serializable for both Android Intent passing and Firestore compatibility.
 */
public class UserList implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<User> users;
    private Integer capacity; // null means unlimited

    // Required empty constructor for Firestore
    public UserList() {
        this.users = new ArrayList<>();
        this.capacity = null;
    }

    public UserList(Integer capacity) {
        this.users = new ArrayList<>();
        this.capacity = capacity;
    }

    public UserList(ArrayList<User> users, Integer capacity) {
        this.users = users != null ? users : new ArrayList<>();
        this.capacity = capacity;
    }

    // Getters and setters (required for Firestore)
    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    // Utility methods
    public void add(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
        }
    }

    public void remove(User user) {
        users.remove(user);
    }

    public boolean contains(User user) {
        return users.contains(user);
    }

    public int size() {
        return users.size();
    }

    public User get(int index) {
        return users.get(index);
    }

    public void clear() {
        users.clear();
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    public boolean isFull() {
        return capacity != null && users.size() >= capacity;
    }
}