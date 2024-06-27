package com.codex.tala;

import static com.codex.tala.CalendarUtils.daysInWeekArray;
import static com.codex.tala.CalendarUtils.monthYearFromDate;
import static com.codex.tala.CalendarUtils.selectedDate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DayFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView hourListView;
    private final String userId;
    private View view;

    public DayFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_day, container, false);
        initWidgets();
        setBtn();
        setWeekView();
        setAllDayEvents();
        return view;
    }

    private void initWidgets() {
        hourListView = (ListView) view.findViewById(R.id.hourListView);
        calendarRecyclerView = (RecyclerView) view.findViewById(R.id.calendarDayRecyclerView);
        monthYearText = (TextView) view.findViewById(R.id.tv_daymonthYear);
    }

    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(getContext(), userId, days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setAdapter(calendarAdapter);
        calendarRecyclerView.setLayoutManager(layoutManager);
        setHourAdapter();
        setAllDayEvents();
    }

    private void setHourAdapter() {
        fetchHourEvents(new HourEventsCallback() {
            @Override
            public void onHourEventsFetched(List<HourEvent> hourEvents) {
                HourAdapter hourAdapter = new HourAdapter(getContext(), hourEvents);
                hourListView.setAdapter(hourAdapter);
            }
        });
    }

    private void fetchHourEvents(HourEventsCallback callback) {
        ArrayList<HourEvent> list = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            LocalTime time = LocalTime.of(hour, 0);
            Event.eventsForDateAndTime(userId, selectedDate, time, new Event.OnEventsFetchedListener() {
                @Override
                public void onEventsFetched(ArrayList<Event> events) {
                    HourEvent hourEvent = new HourEvent(userId, time, events);
                    list.add(hourEvent);

                    if (list.size() == 24) {
                        callback.onHourEventsFetched(list);
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    interface HourEventsCallback {
        void onHourEventsFetched(List<HourEvent> hourEvents);
    }

    private List<HourEvent> hourEventList() {
        ArrayList<HourEvent> list = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            LocalTime time = LocalTime.of(hour, 0);
            Event.eventsForDateAndTime(userId, selectedDate, time, new Event.OnEventsFetchedListener() {
                @Override
                public void onEventsFetched(ArrayList<Event> events) {
                    HourEvent hourEvent = new HourEvent(userId, time, events);
                    list.add(hourEvent);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return list;
    }

    public void setBtn() {
        Button nextBTN = view.findViewById(R.id.nextWeekAction);
        Button prevBTN = view.findViewById(R.id.prevWeekAction);

        prevBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousWeekAction();
            }
        });

        nextBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextWeekAction();
            }
        });
    }

    public void previousWeekAction() {
        selectedDate = selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction() {
        selectedDate = selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        selectedDate = date;
        setWeekView();
    }

    private void setAllDayEvents() {
        TextView event1 = view.findViewById(R.id.allday_event1);
        TextView event2 = view.findViewById(R.id.allday_event2);
        TextView event3 = view.findViewById(R.id.allday_event3);

        Event.eventsForDate(userId, selectedDate, new Event.OnEventsFetchedListener() {
            @Override
            public void onEventsFetched(ArrayList<Event> events) {
                ArrayList<Event> allDayEvents = new ArrayList<>();
                for (Event event : events) {
                    if (!selectedDate.toString().equals(event.getEndDate()) || !selectedDate.toString().equals(event.getStartDate())) {
                        allDayEvents.add(event);
                    }
                }
                if (allDayEvents.size() == 0) {
                    hideEvent(event1);
                    hideEvent(event2);
                    hideEvent(event3);
                } else {
                    if (allDayEvents.size() == 1) {
                        setEvent(event1, allDayEvents.get(0));
                        hideEvent(event2);
                        hideEvent(event3);
                    } else if (allDayEvents.size() == 2) {
                        setEvent(event1, allDayEvents.get(0));
                        setEvent(event2, allDayEvents.get(1));
                        hideEvent(event3);
                    } else if (allDayEvents.size() == 3) {
                        setEvent(event1, allDayEvents.get(0));
                        setEvent(event2, allDayEvents.get(1));
                        setEvent(event3, allDayEvents.get(2));
                    } else {
                        setEvent(event1, allDayEvents.get(0));
                        setEvent(event2, allDayEvents.get(1));
                        event3.setVisibility(View.VISIBLE);
                        String eventsNotShown = String.valueOf(allDayEvents.size() - 2);
                        eventsNotShown += " More Event/s";
                        event3.setText(eventsNotShown);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setEvent(TextView textView, Event event) {
        if (!Objects.equals(event.getColor(), "Default color")) {
            textView.setBackgroundColor(ContextCompat.getColor(requireContext(), getResources().getIdentifier(event.getColor(), "color", requireContext().getPackageName())));
        }
        textView.setText(event.getTitle().isEmpty() ? "(No title)" : event.getTitle());
        textView.setVisibility(View.VISIBLE);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivityEventDetails.class);
                intent.putExtra("eventId", event.getEventID());
                intent.putExtra("userId", event.getUserId());
                if (getContext() instanceof Activity) {
                    Activity activity = (Activity) getContext();
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_up_anim, 0);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            }
        });
    }

    private void hideEvent(TextView tv) {
        tv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setWeekView();
    }
}