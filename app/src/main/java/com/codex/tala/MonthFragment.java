package com.codex.tala;

import static com.codex.tala.CalendarUtils.daysInMonthArray;
import static com.codex.tala.CalendarUtils.monthYearFromDate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;

public class MonthFragment extends Fragment implements CalendarAdapter.OnItemListener{

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;
    private String userId;

    public MonthFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_month, container, false);
        initWidgets(view);
        setBtn(view);
        setMonthView();

        return view;
    }

    private void initWidgets(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.tv_monthYear);
        eventListView = view.findViewById(R.id.eventListView);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray();
        CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), userId, daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setLayoutManager(layoutManager);
        setEventAdapter();
    }

    public void setBtn(View view) {
        Button nextBTN = view.findViewById(R.id.nextMonthAction);
        Button prevBTN = view.findViewById(R.id.prevMonthAction);

        prevBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMonthAction();
            }
        });

        nextBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonthAction();
            }
        });
    }

    public void previousMonthAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if (date != null) {
            CalendarUtils.selectedDate = date;
            setMonthView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setMonthView();
    }

    private void setEventAdapter() {
        Event.eventsForDate(userId, CalendarUtils.selectedDate, new Event.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(ArrayList<Event> events) {
                EventAdapter eventAdapter = new EventAdapter(requireContext().getApplicationContext(), events);
                eventListView.setAdapter(eventAdapter);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                // Handle the error, maybe show a message to the user
            }
        });
    }
}
