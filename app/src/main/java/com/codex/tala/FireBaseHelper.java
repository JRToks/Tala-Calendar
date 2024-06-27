package com.codex.tala;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FireBaseHelper {

    public static CompletableFuture<Boolean> insertEventData(String userId, String title, String startDate, String endDate,
                                                             String startTime, String endTime, String recurring, String notif,
                                                             String color, String description) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");

        HashMap<String, Object> eventData = new HashMap<>();
        eventData.put("uid", userId);
        eventData.put("title", title);
        eventData.put("startDate", startDate);
        eventData.put("endDate", endDate);
        eventData.put("startTime", startTime);
        eventData.put("endTime", endTime);
        eventData.put("recurring", recurring);
        eventData.put("notification", notif);
        eventData.put("color", color);
        eventData.put("description", description);

        DatabaseReference newEventRef = eventsRef.push();
        // Signal failure
        newEventRef.setValue(eventData)
                .addOnSuccessListener(aVoid -> {
                    future.complete(true);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public static CompletableFuture<Map<String, Object>> searchEvent(String userId, String title, String startDate, String endDate, String startTime, String endTime) {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Default empty string parameters to null
        title = (title != null && !title.isEmpty()) ? title : null;
        startDate = (startDate != null && !startDate.isEmpty()) ? startDate : null;
        endDate = (endDate != null && !endDate.isEmpty()) ? endDate : null;
        startTime = (startTime != null && !startTime.isEmpty()) ? startTime : null;
        endTime = (endTime != null && !endTime.isEmpty()) ? endTime : null;

        // Initialize the query
        Query query = eventsRef.orderByChild("uid").equalTo(userId);

        if (title != null) {
            query = query.orderByChild("title").startAt(title).endAt(title + "\uf8ff");
        }

        if (startDate != null) {
            query = query.orderByChild("startDate").equalTo(startDate);
        }

        if (endDate != null) {
            query = query.orderByChild("endDate").equalTo(endDate);
        }

        if (startTime != null) {
            query = query.orderByChild("startTime").equalTo(startTime);
        }

        if (endTime != null) {
            query = query.orderByChild("endTime").equalTo(endTime);
        }

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> eventList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String eventId = snapshot.getKey(); // Get the event ID
                    Map<String, Object> event = (Map<String, Object>) snapshot.getValue();
                    event.put("eventId", eventId); // Add event ID to the event map
                    eventList.add(event);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("events", eventList);
                future.complete(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }


//    public Map<String, Object> searchEvent(String userId, String title, String startDate, String endDate, String startTime, String endTime) {
//        List<Map<String, Object>> eventList = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        // Default empty string parameters to null
//        title = (title != null && !title.isEmpty()) ? title : null;
//        startDate = (startDate != null && !startDate.isEmpty()) ? startDate : null;
//        endDate = (endDate != null && !endDate.isEmpty()) ? endDate : null;
//        startTime = (startTime != null && !startTime.isEmpty()) ? startTime : null;
//        endTime = (endTime != null && !endTime.isEmpty()) ? endTime : null;
//
//        String query = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + COLUMN_USER_ID + " = ?";
//        ArrayList<String> selectionArgsList = new ArrayList<>();
//        selectionArgsList.add(String.valueOf(userId));
//
//        if (title != null) {
//            query += " AND " + COLUMN_EVENT_TITLE + " LIKE ?";
//            selectionArgsList.add("%" + title + "%");
//        }
//
//        if (startDate != null && endDate != null) {
//            query += " AND ((" + COLUMN_START_DATE + " BETWEEN ? AND ?) OR (" + COLUMN_END_DATE + " BETWEEN ? AND ?))";
//            selectionArgsList.add(startDate);
//            selectionArgsList.add(endDate);
//            selectionArgsList.add(startDate);
//            selectionArgsList.add(endDate);
//        } else if (startDate != null) {
//            query += " AND (" + COLUMN_START_DATE + " = ?)";
//            selectionArgsList.add(startDate);
//            selectionArgsList.add(startDate);
//        } else if(endDate != null){
//            query += " AND (" + COLUMN_END_DATE + " = ?)";
//            selectionArgsList.add(endDate);
//            selectionArgsList.add(endDate);
//        }
//
//        if (startTime != null && endTime != null) {
//            query += " AND ((" + COLUMN_START_TIME + " BETWEEN ? AND ?) OR (" + COLUMN_END_TIME + " BETWEEN ? AND ?))";
//            selectionArgsList.add(startTime);
//            selectionArgsList.add(endTime);
//            selectionArgsList.add(startTime);
//            selectionArgsList.add(endTime);
//        } else if (startTime != null) {
//            query += " AND (" + COLUMN_START_TIME + " = ?)";
//            selectionArgsList.add(startTime);
//            selectionArgsList.add(startTime);
//        } else if (endTime != null){
//            query += " AND (" + COLUMN_END_TIME + " = ?)";
//            selectionArgsList.add(endTime);
//            selectionArgsList.add(endTime);
//        }
//
//        // Convert selectionArgsList to an array
//        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
//
//        // Execute the query
//        Cursor cursor = db.rawQuery(query, selectionArgs);
//
//        // Iterate over the cursor to build the event list
//        if (cursor != null && cursor.moveToFirst()) {
//            try {
//                do {
//                    Map<String, Object> event = new HashMap<>();
//                    event.put(COLUMN_EVENT_ID, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)));
//                    event.put(COLUMN_EVENT_TITLE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE)));
//                    event.put(COLUMN_START_DATE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)));
//                    event.put(COLUMN_END_DATE, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE)));
//                    event.put(COLUMN_START_TIME, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
//                    event.put(COLUMN_END_TIME, cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
//
//                    eventList.add(event);
//                } while (cursor.moveToNext());
//            } finally {
//                cursor.close();
//            }
//        }
//
//        // Create the final result map
//        Map<String, Object> result = new HashMap<>();
//        result.put("events", eventList);
//
//        return result;
//    }

//    public boolean editEventData(String userId, int eventId, String title, String startDate, String endDate, String startTime, String endTime, String description) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        boolean isUpdated = false;
//        if (eventId != 0){
//            title = (title != null && !title.isEmpty()) ? title : null;
//            startDate = (startDate != null && !startDate.isEmpty()) ? startDate : null;
//            endDate = (endDate != null && !endDate.isEmpty()) ? endDate : null;
//            startTime = (startTime != null && !startTime.isEmpty()) ? startTime : null;
//            endTime = (endTime != null && !endTime.isEmpty()) ? endTime : null;
//            description = (description != null && !description.isEmpty()) ? description : null;
//
//            try {
//                ContentValues newValues = new ContentValues();
//
//                if (title != null) {
//                    newValues.put(COLUMN_EVENT_TITLE, title);
//                }
//                if (startDate != null) {
//                    newValues.put(COLUMN_START_DATE, startDate);
//                }
//                if (endDate != null) {
//                    newValues.put(COLUMN_END_DATE, endDate);
//                }
//                if (startTime != null) {
//                    newValues.put(COLUMN_START_TIME, startTime);
//                }
//                if (endTime != null) {
//                    newValues.put(COLUMN_END_TIME, endTime);
//                }
//                if (description != null) {
//                    newValues.put(COLUMN_DESCRIPTION, description);
//                }
//
//                String selection = COLUMN_USER_ID + " = ? AND " + COLUMN_EVENT_ID + "= ?";
//                String[] selectionArgs = {String.valueOf(userId), String.valueOf(eventId)};
//
//                int rowsUpdated = db.update(TABLE_EVENTS, newValues, selection, selectionArgs);
//                Log.d("params", userId + " | " + eventId + " | " + title + " | " + startDate + " | " + endDate + " | " + startTime + " | " + endTime + " | " + description);
//
//                if (rowsUpdated > 0) {
//                    isUpdated = true;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                db.close();
//            }
//        }
//
//        return isUpdated;
//    }

//    public boolean deleteEventData(String userId, int eventId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        boolean isDeleted = false;
//        try {
//            String selection = COLUMN_USER_ID + " = ? AND " + COLUMN_EVENT_ID + " = ?";
//            String[] selectionArgs = {String.valueOf(userId), String.valueOf(eventId)};
//
//            int rowsDeleted = db.delete(TABLE_EVENTS, selection, selectionArgs);
//
//            if (rowsDeleted > 0) {
//                isDeleted = true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            db.close();
//        }
//        return isDeleted;
//    }
}
