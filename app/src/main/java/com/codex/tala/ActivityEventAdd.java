package com.codex.tala;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ActivityEventAdd extends AppCompatActivity {
    private LinearLayout changeColor;
    private ImageView circleColor;
    private EditText eventNameET, descriptionET;
    private TextView dateStartTv, dateEndTv, timeStartTv, timeEndTv, colorNameTv;
    private DatePickerDialog.OnDateSetListener mStartDateSetListener, mEndDateSetListener;
    private DBHelper db;
    private LocalDate startDateVal, endDateVal;
    private int year, month, day, userId;


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        db = new DBHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        startDateVal = endDateVal = CalendarUtils.selectedDate;
        year = startDateVal.getYear();
        month = startDateVal.getMonthValue()-1;
        day = startDateVal.getDayOfMonth();

        changeColor = findViewById(R.id.lin_changeColor);
        circleColor = findViewById(R.id.circleColor);
        colorNameTv = findViewById(R.id.colorNameTV);
        eventNameET = findViewById(R.id.eventNameET);
        dateStartTv = (TextView) findViewById(R.id.dateStartTv);
        dateEndTv = (TextView) findViewById(R.id.dateEndTv);
        timeStartTv = (TextView) findViewById(R.id.timeStartTv);
        timeEndTv = (TextView) findViewById(R.id.timeEndTv);
        descriptionET = (EditText) findViewById(R.id.descriptionET);

        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        Button addBtn = (Button) findViewById(R.id.add_btn_event);

        setTextDate();
        setTextStart();
        setTextEnd();
        startDate();
        endDate();
        startTime();
        endTime();
        setColor();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = eventNameET.getText().toString();
                String startTime = timeStartTv.getText().toString();
                String endTime = timeEndTv.getText().toString();
                String color = colorNameTv.getText().toString();
                String description = descriptionET.getText().toString();

                LocalTime sT = LocalTime.parse(CalendarUtils.convert12to24(startTime));
                LocalTime eT = LocalTime.parse(CalendarUtils.convert12to24(endTime));
                if (startDateVal.isAfter(endDateVal) || (sT.isAfter(eT) && startDateVal.isEqual(endDateVal))){
                    Toast.makeText(ActivityEventAdd.this, "The event end time cannot be set before the start time.", Toast.LENGTH_SHORT).show();
                    dateEndTv.setTextColor(Color.RED);
                    timeEndTv.setTextColor(Color.RED);
                }else{
                    boolean insert = db.insertEventData(userId, eventName, startDateVal.toString(), endDateVal.toString(), startTime, endTime, null, null, color, description); //runs the inserteventdata function in the dbhelper class
                    if (insert) {
                        db.close();
                        finish();
                        overridePendingTransition(0,R.anim.slide_down_anim);
                    } else {
                        Toast.makeText(ActivityEventAdd.this, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.close();
                finish();
                overridePendingTransition(0,R.anim.slide_down_anim);
            }
        });
    }

    private void setColor() {
        changeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEventAdd.this);
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
                        ActivityEventAdd.this,
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
                        ActivityEventAdd.this,
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
                TimePickerDialog dialog = new TimePickerDialog(ActivityEventAdd.this, new TimePickerDialog.OnTimeSetListener() {
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
                TimePickerDialog dialog = new TimePickerDialog(ActivityEventAdd.this, new TimePickerDialog.OnTimeSetListener() {
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
