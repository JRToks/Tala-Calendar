package com.codex.tala;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ActivityEventDetails extends AppCompatActivity {
    private TextView title, description, startDate, endDate, startTime, endTime, monthTv, colorTv, notifTv, repeatTv;
    private ImageView editColor;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference eventRef;
    private LinearLayout linearLayout;
    private Button edit_event_btn, delete_event_btn;
    private String eventid;
    private AlertDialog.Builder builder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        eventid = getIntent().getStringExtra("eventId");
        if (eventid == null) {
            finish();
            return;
        }

        eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventid);

        linearLayout = findViewById(R.id.lin_back_btn);
        monthTv = findViewById(R.id.ed_month_btn);
        edit_event_btn = findViewById(R.id.edit_event_btn);
        delete_event_btn = findViewById(R.id.delete_event_btn);
        builder = new AlertDialog.Builder(this);

        title = findViewById(R.id.ED_event_name);
        description = findViewById(R.id.descriptionTV);
        startDate = findViewById(R.id.ED_dateStartTv);
        endDate = findViewById(R.id.ED_dateEndTv);
        startTime = findViewById(R.id.ED_timeStartTv);
        endTime = findViewById(R.id.ED_timeEndTv);
        repeatTv = findViewById(R.id.EDrepeatTv);
        notifTv = findViewById(R.id.EDnotificationTv);
        colorTv = findViewById(R.id.editColorTv);
        editColor = findViewById(R.id.editColor);

        setEventDetails();
        setupBtnClickListeners();
    }

    private void setupBtnClickListeners() {
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.slide_down_anim);
            }
        });

        edit_event_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEventDetails.this, ActivityEventEdit.class);
                intent.putExtra("eventId", eventid);
                startActivity(intent);
                finish();
            }
        });

        delete_event_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setTitle("")
                        .setMessage("Are you sure you want to delete this event?")
                        .setCancelable(true)
                        .setPositiveButton("Delete Event", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                eventRef.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ActivityEventDetails.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                        overridePendingTransition(0, R.anim.slide_down_anim);
                                    } else {
                                        Toast.makeText(ActivityEventDetails.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
    }

    private void setEventDetails() {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events")
                .child(eventid);

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch each field from the snapshot
                    String titleCheck = snapshot.child("title").getValue(String.class);
                    if (titleCheck == null || titleCheck.isEmpty())
                        titleCheck = "(No title)";
                    title.setText(titleCheck);

                    String descCheck = snapshot.child("description").getValue(String.class);
                    if (descCheck == null || descCheck.isEmpty())
                        descCheck = "Add description";
                    description.setText(descCheck);

                    String rpt = snapshot.child("recurring").getValue(String.class);
                    if (rpt != null) {
                        repeatTv.setText(rpt);
                    }

                    String notif = snapshot.child("notification").getValue(String.class);
                    if (notif != null && !notif.isEmpty()) {
                        notifTv.setText(notif);
                    }

                    String color = snapshot.child("color").getValue(String.class);
                    Map<String, Integer> colorMap = new HashMap<>();
                    colorMap.put("Tomato", R.drawable.color_circle_red);
                    colorMap.put("Tangerine", R.drawable.color_circle_orange);
                    colorMap.put("Banana", R.drawable.color_circle_yellow);
                    colorMap.put("Basil", R.drawable.color_circle_green);
                    colorMap.put("Flamingo", R.drawable.color_circle_flamingo);
                    colorMap.put("Graphite", R.drawable.color_circle_gray);
                    colorMap.put("Grape", R.drawable.color_circle_purple);

                    Integer colorResourceId = colorMap.get(color);
                    if (colorResourceId != null) {
                        editColor.setImageResource(colorResourceId);
                        colorTv.setText(color);
                    }

                    String startDateStr = snapshot.child("startDate").getValue(String.class);
                    if (startDateStr != null) {
                        String formattedStartDate = CalendarUtils.convertDateFormat(startDateStr);
                        startDate.setText(formattedStartDate);
                    }

                    String endDateStr = snapshot.child("endDate").getValue(String.class);
                    if (endDateStr != null) {
                        String formattedEndDate = CalendarUtils.convertDateFormat(endDateStr);
                        endDate.setText(formattedEndDate);
                    }

                    String startTimeStr = snapshot.child("startTime").getValue(String.class);
                    if (startTimeStr != null) {
                        startTime.setText(startTimeStr);
                    }

                    String endTimeStr = snapshot.child("endTime").getValue(String.class);
                    if (endTimeStr != null) {
                        endTime.setText(endTimeStr);
                    }

                    monthTv.setText(CalendarUtils.monthFromDate(startDateStr));
                } else {
                    Toast.makeText(ActivityEventDetails.this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data", error.toException());
                Toast.makeText(ActivityEventDetails.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
