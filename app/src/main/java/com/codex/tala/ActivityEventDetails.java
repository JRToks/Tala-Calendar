package com.codex.tala;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActivityEventDetails extends AppCompatActivity {
    private TextView title, description, startDate, endDate, startTime, endTime, monthTv, colorTv, notifTv, repeatTv;
    private ImageView editColor;
    private DBHelper db;
    private LinearLayout linearLayout;
    private Button edit_event_btn, delete_event_btn;
    private int eventid;
    private String userid;
    AlertDialog.Builder builder;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        userid = getIntent().getStringExtra("userId");
        eventid = getIntent().getIntExtra("eventId", -1);

        linearLayout = (LinearLayout) findViewById(R.id.lin_back_btn);
        monthTv = (TextView) findViewById(R.id.ed_month_btn);
        edit_event_btn = (Button) findViewById(R.id.edit_event_btn);
        delete_event_btn = (Button) findViewById(R.id.delete_event_btn);
        builder = new AlertDialog.Builder(this);

        title = (TextView) findViewById(R.id.ED_event_name);
        description = (TextView) findViewById(R.id.descriptionTV);
        startDate = (TextView) findViewById(R.id.ED_dateStartTv);
        endDate = (TextView) findViewById(R.id.ED_dateEndTv);
        startTime = (TextView) findViewById(R.id.ED_timeStartTv);
        endTime = (TextView) findViewById(R.id.ED_timeEndTv);
        repeatTv = (TextView) findViewById(R.id.EDrepeatTv);
        notifTv = (TextView) findViewById(R.id.EDnotificationTv);
        colorTv = (TextView) findViewById(R.id.editColorTv);
        editColor = (ImageView) findViewById(R.id.editColor);

        db = new DBHelper(this);
        setEventDetails();
        setupBtnClickListeners();
        db.close();
    }
    private void setupBtnClickListeners() {
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.close();
                finish();
                overridePendingTransition(0,R.anim.slide_down_anim);
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
                                boolean isDeleted = db.deleteEventData(userid, eventid);
                                if (isDeleted) {
                                    Toast.makeText(ActivityEventDetails.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                    overridePendingTransition(0,R.anim.slide_down_anim);
                                } else {
                                    Toast.makeText(ActivityEventDetails.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                }
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
        Cursor cursor = db.getEventData(userid, eventid);
        if (cursor != null && cursor.moveToFirst()) {
            String titleCheck = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EVENT_TITLE));
            if (titleCheck.isEmpty())
                titleCheck = "(No title)";
            title.setText(titleCheck);

            String descCheck = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPTION));
            if (descCheck.isEmpty())
                descCheck = "Add description";
            description.setText(descCheck);

            String rpt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_RECURRING));
            repeatTv.setText(rpt);

            String notif = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOTIFICATION));
            if (!Objects.equals(notif, "")){
                notifTv.setText(notif);
            }

            String color = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_COLOR));
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

            String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_START_DATE));
            String formattedStartDate = CalendarUtils.convertDateFormat(startDateStr);
            startDate.setText(formattedStartDate);

            String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_END_DATE));
            String formattedEndDate = CalendarUtils.convertDateFormat(endDateStr);
            endDate.setText(formattedEndDate);

            startTime.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_START_TIME)));
            endTime.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_END_TIME)));
            monthTv.setText(CalendarUtils.monthFromDate(startDateStr));
            cursor.close();
        }
    }
}
