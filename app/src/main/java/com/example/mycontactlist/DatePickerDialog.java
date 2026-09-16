package com.example.mycontactlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialog extends DialogFragment {

    private Calendar selectedDate;

    public interface SaveDateListener {
        void didFinishDatePickerDialog(Calendar selectedTime);
    }

    public DatePickerDialog() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.select_date,
                container,
                false
        );

        if (getDialog() != null) {
            getDialog().setTitle("Select Date");
        }

        selectedDate = Calendar.getInstance();

        CalendarView calendarView =
                view.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(
                (calendarView1, year, month, dayOfMonth) -> selectedDate.set(
                        year,
                        month,
                        dayOfMonth
                ));

        Button selectButton =
                view.findViewById(R.id.buttonSelect);

        selectButton.setOnClickListener(
                view1 -> saveItem(selectedDate));

        Button cancelButton =
                view.findViewById(R.id.buttonCancel);

        cancelButton.setOnClickListener(
                view2 -> dismiss());

        return view;
    }

    private void saveItem(Calendar selectedTime) {
        SaveDateListener activity =
                (SaveDateListener) getActivity();

        if (activity != null) {
            activity.didFinishDatePickerDialog(selectedTime);
        }

        dismiss();
    }
}
