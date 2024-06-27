package com.codex.tala;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ActivityEventEdit extends AppCompatActivity {
    private LinearLayout changeColor, lin_addNotif, lin_DNR;
    private ImageView circleColor;
    private EditText eventNameET, descriptionET;
    private TextView dateStartTv, dateEndTv, timeStartTv, timeEndTv, colorNameTv, repeatTv, notificationTv;
    private DatePickerDialog.OnDateSetListener mStartDateSetListener, mEndDateSetListener;
    private LocalDate startDateVal, endDateVal;
    private int year, month, day;
    private String eventid;
    private DatabaseReference  eventRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        eventid = getIntent().getStringExtra("eventId");
        if (eventid == null) {
            finish();
            return;
        }

        eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventid);

        startDateVal = endDateVal = CalendarUtils.selectedDate;
        year = startDateVal.getYear();
        month = startDateVal.getMonthValue()-1;
        day = startDateVal.getDayOfMonth();

        notificationTv = findViewById(R.id.EE_notificationTv);
        repeatTv = findViewById(R.id.EE_repeatTv);
        changeColor = findViewById(R.id.EE_lin_changeColor);
        lin_addNotif = findViewById(R.id.EE_lin_addNotif);
        lin_DNR = findViewById(R.id.EE_lin_DNR);
        circleColor = findViewById(R.id.EE_circleColor);
        colorNameTv = findViewById(R.id.EE_colorNameTV);
        eventNameET = findViewById(R.id.EE_eventNameET);
        eventNameET.requestFocus();
        dateStartTv = (TextView) findViewById(R.id.EE_dateStartTv);
        dateEndTv = (TextView) findViewById(R.id.EE_dateEndTv);
        timeStartTv = (TextView) findViewById(R.id.EE_timeStartTv);
        timeEndTv = (TextView) findViewById(R.id.EE_timeEndTv);
        descriptionET = (EditText) findViewById(R.id.EE_descriptionET);

        Button cancelBtn = (Button) findViewById(R.id.EE_cancel_btn);
        Button saveBtn = (Button) findViewById(R.id.EE_save_btn_event);

        setEventDetails();
        setTextDate();
        setTextStart();
        setTextEnd();
        startDate();
        endDate();
        startTime();
        endTime();
        setColor();
        setupRepeatOptions();
        setupNotificationOptions();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }

        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,R.anim.slide_down_anim);
            }
        });
    }

    private void setEventDetails() {
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String titleCheck = snapshot.child("title").getValue(String.class);
                    if (titleCheck == null || titleCheck.isEmpty()) titleCheck = "(No title)";
                    eventNameET.setText(titleCheck);

                    String descCheck = snapshot.child("description").getValue(String.class);
                    if (descCheck == null || descCheck.isEmpty()) descCheck = "Add description";
                    descriptionET.setText(descCheck);

                    String rpt = snapshot.child("recurring").getValue(String.class);
                    if (rpt != null) repeatTv.setText(rpt);

                    String notif = snapshot.child("notification").getValue(String.class);
                    if (notif != null && !notif.isEmpty()) notificationTv.setText(notif);

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
                        circleColor.setImageResource(colorResourceId);
                        colorNameTv.setText(color);
                    }

                    String startDateStr = snapshot.child("startDate").getValue(String.class);
                    if (startDateStr != null) {
                        startDateVal = LocalDate.parse(startDateStr);
                        String formattedStartDate = CalendarUtils.convertDateFormat(startDateStr);
                        dateStartTv.setText(formattedStartDate);
                    }

                    String endDateStr = snapshot.child("endDate").getValue(String.class);
                    if (endDateStr != null) {
                        endDateVal = LocalDate.parse(endDateStr);
                        String formattedEndDate = CalendarUtils.convertDateFormat(endDateStr);
                        dateEndTv.setText(formattedEndDate);
                    }

                    String startTimeStr = snapshot.child("startTime").getValue(String.class);
                    if (startTimeStr != null) timeStartTv.setText(startTimeStr);

                    String endTimeStr = snapshot.child("endTime").getValue(String.class);
                    if (endTimeStr != null) timeEndTv.setText(endTimeStr);

                    dateStartTv.setText(CalendarUtils.convertDateFormat(startDateStr));
                    dateEndTv.setText(CalendarUtils.convertDateFormat(endDateStr));
                } else {
                    Toast.makeText(ActivityEventEdit.this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityEventEdit.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEvent() {
        String eventName = eventNameET.getText().toString();
        String startTime = timeStartTv.getText().toString();
        String endTime = timeEndTv.getText().toString();
        String notif = notificationTv.getText().toString();
        String recurr = repeatTv.getText().toString();
        String color = colorNameTv.getText().toString();
        String description = descriptionET.getText().toString();

        LocalTime sT = LocalTime.parse(CalendarUtils.convert12to24(startTime));
        LocalTime eT = LocalTime.parse(CalendarUtils.convert12to24(endTime));
        if (startDateVal.isAfter(endDateVal) || (sT.isAfter(eT) && startDateVal.isEqual(endDateVal))) {
            Toast.makeText(ActivityEventEdit.this, "The event end time cannot be set before the start time.", Toast.LENGTH_SHORT).show();
            dateEndTv.setTextColor(Color.RED);
            timeEndTv.setTextColor(Color.RED);
        } else {
            HashMap<String, Object> eventData = new HashMap<>();
            eventData.put("title", eventName);
            eventData.put("startDate", startDateVal.toString());
            eventData.put("endDate", endDateVal.toString());
            eventData.put("startTime", startTime);
            eventData.put("endTime", endTime);
            eventData.put("recurring", recurr);
            eventData.put("notification", notif);
            eventData.put("color", color);
            eventData.put("description", description);

            eventRef.updateChildren(eventData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivityEventEdit.this, "Event updated successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(0, R.anim.slide_down_anim);
                    } else {
                        Toast.makeText(ActivityEventEdit.this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupNotificationOptions() {
        lin_addNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEventEdit.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.notification_options_dialog, null);
                builder.setView(dialogView);

                final RadioGroup repeatOptionsRadioGroup = dialogView.findViewById(R.id.notificationOptionsRadioGroup);

                final AlertDialog dialog = builder.create();

                repeatOptionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedRadioButton = dialogView.findViewById(checkedId);
                        String repeatOption = selectedRadioButton.getText().toString();
                        if (repeatOption.equals("None")){
                            repeatOption = "Add a notification";
                            notificationTv.setTextColor(Color.GRAY);
                        }else{
                            notificationTv.setTextColor(Color.BLACK);
                        }
                        notificationTv.setText(repeatOption);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    private void setupRepeatOptions() {
        lin_DNR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEventEdit.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.repeat_options_dialog, null);
                builder.setView(dialogView);

                final RadioGroup repeatOptionsRadioGroup = dialogView.findViewById(R.id.repeatOptionsRadioGroup);

                final AlertDialog dialog = builder.create();

                repeatOptionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedRadioButton = dialogView.findViewById(checkedId);
                        String repeatOption = selectedRadioButton.getText().toString();
                        repeatTv.setText(repeatOption);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    private void setColor() {
        changeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEventEdit.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.color_picker_dialog, null);
                builder.setView(dialogView);

                final AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.color1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_red);
                        colorNameTv.setText("Tomato");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_orange);
                        colorNameTv.setText("Tangerine");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_yellow);
                        colorNameTv.setText("Banana");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color4).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_green);
                        colorNameTv.setText("Basil");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color5).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_purple);
                        colorNameTv.setText("Grape");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color6).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_flamingo);
                        colorNameTv.setText("Flamingo");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color7).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.color_circle_gray);
                        colorNameTv.setText("Graphite");
                        dialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.color8).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        circleColor.setImageResource(R.drawable.baseline_darkcyan_circle_24);
                        colorNameTv.setText("Default Color");
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }
        });
    }

    private void startDate(){
        dateStartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(
                        ActivityEventEdit.this,
                        mStartDateSetListener,
                        year, month, day);

                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();
            }
        });

        mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                startDateVal = LocalDate.of(year, month+1, dayOfMonth);

                SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                String formattedDate = format.format(calendar.getTime());

                dateStartTv.setText(formattedDate);
            }
        };
    }

    private void endDate(){
        dateEndTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(
                        ActivityEventEdit.this,
                        mEndDateSetListener,
                        year, month, day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();
            }
        });

        mEndDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                endDateVal = LocalDate.of(year, month+1, dayOfMonth);

                SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                String formattedDate = format.format(calendar.getTime());

                dateEndTv.setText(formattedDate);
            }
        };
    }

    private void startTime(){
        timeStartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int hourForDialog = currentHour + 1;
                TimePickerDialog dialog = new TimePickerDialog(ActivityEventEdit.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hours, int minutes) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hours);
                        calendar.set(Calendar.MINUTE, minutes);

                        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        String formattedTime = format.format(calendar.getTime());

                        timeStartTv.setText(formattedTime);
                    }
                }, hourForDialog, 0, false);
                dialog.show();
            }
        });
    }

    private void endTime() {
        timeEndTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int hourForDialog = currentHour + 2;
                TimePickerDialog dialog = new TimePickerDialog(ActivityEventEdit.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hours, int minutes) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, hours);
                        calendar.set(Calendar.MINUTE, minutes);

                        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        String formattedTime = format.format(calendar.getTime());

                        timeEndTv.setText(formattedTime);
                    }
                }, hourForDialog, 0, false);
                dialog.show();
            }
        });
    }

    private void setTextStart(){
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int startHour = currentHour + 1;

        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startCalendar.set(Calendar.MINUTE, 0); // Assuming minutes are always 0 for the start time

        String formattedStartTime = format.format(startCalendar.getTime());

        timeStartTv.setText(formattedStartTime);
    }

    private void setTextEnd() {
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int endHour = currentHour +2;

        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        startCalendar.set(Calendar.MINUTE, 0); // Assuming minutes are always 0 for the start time

        String formattedStartTime = format.format(startCalendar.getTime());

        timeEndTv.setText(formattedStartTime);
    }

    private void setTextDate() {
        LocalDate selectedDate = CalendarUtils.selectedDate;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault());
        String formattedDate = selectedDate.format(formatter);

        dateStartTv.setText(formattedDate);
        dateEndTv.setText(formattedDate);
    }
}
