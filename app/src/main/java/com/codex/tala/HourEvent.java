package com.codex.tala;

import java.time.LocalTime;
import java.util.ArrayList;

public class HourEvent {
    private LocalTime time;
    private final ArrayList<Event> events;
    private String userId;

    public HourEvent(String userId, LocalTime time, ArrayList<Event> events){
        this.userId = userId;
        this.time = time;
        this.events = events;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }
}
