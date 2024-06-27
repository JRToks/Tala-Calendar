package com.codex.tala;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Event {
    public static ArrayList<Event> eventsList = new ArrayList<>();

    public static void eventsForDate(String userId, LocalDate date, final OnEventsFetchedListener listener) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        String dateString = date.toString();

        eventsRef.orderByChild("uid").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Event> events = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String startDate = eventSnapshot.child("startDate").getValue(String.class);
                    String endDate = eventSnapshot.child("endDate").getValue(String.class);

                    if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
                        LocalDate eventStartDate = LocalDate.parse(startDate);
                        LocalDate eventEndDate = LocalDate.parse(endDate);

                        if (!date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)) {
                            String eventID = eventSnapshot.getKey();
                            String title = eventSnapshot.child("title").getValue(String.class);
                            String startTime = eventSnapshot.child("startTime").getValue(String.class);
                            String endTime = eventSnapshot.child("endTime").getValue(String.class);
                            String color = eventSnapshot.child("color").getValue(String.class);

                            Event event = new Event(eventID, userId, title, startDate, endDate, startTime, endTime, color);
                            events.add(event);
                        }
                    }
                }
                listener.onEventsFetched(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.toException());
            }
        });
    }

    public static void eventsForDateAndTime(String userId, LocalDate date, LocalTime time, final OnEventsFetchedListener listener) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        String dateString = date.toString();
        int cellHour = time.getHour();

        eventsRef.orderByChild("uid").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Event> events = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String startDate = eventSnapshot.child("startDate").getValue(String.class);
                    String startTime = eventSnapshot.child("startTime").getValue(String.class);

                    if (startDate != null && startTime != null && !startDate.isEmpty() && !startTime.isEmpty()) {
                        LocalDate eventStartDate = LocalDate.parse(startDate);
                        String[] st = startTime.split(":");
                        int startHour = Integer.parseInt(st[0]);

                        if (date.equals(eventStartDate) && startHour == cellHour) {
                            String eventID = eventSnapshot.getKey();
                            String title = eventSnapshot.child("title").getValue(String.class);
                            String endDate = eventSnapshot.child("endDate").getValue(String.class);
                            String endTime = eventSnapshot.child("endTime").getValue(String.class);
                            String color = eventSnapshot.child("color").getValue(String.class);

                            Event event = new Event(eventID, userId, title, startDate, endDate, startTime, endTime, color);
                            events.add(event);
                        }
                    }
                }
                listener.onEventsFetched(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.toException());
            }
        });
    }

    public interface OnEventsFetchedListener {
        void onEventsFetched(ArrayList<Event> events);
        void onError(Exception e);
    }

    private final String title, startDate, endDate, startTime, endTime, color;
    private final String userId, eventID;

    public Event(String eventID, String userId, String title, String startDate, String endDate, String startTime, String endTime, String color) {
        this.eventID = eventID;
        this.userId = userId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }

    public String getEventID() {
        return eventID;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getColor() {
        return color;
    }
}