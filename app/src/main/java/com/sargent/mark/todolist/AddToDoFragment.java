package com.sargent.mark.todolist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by mark on 7/4/17.
 */

public class AddToDoFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private EditText toDo;
    private DatePicker dp;
    private Button add;

    //added a variable for category to store the category option selected in the spinner.
    private String category;

    private final String TAG = "addtodofragment";

    public AddToDoFragment() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        category = (String) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //To have a way for the activity to get the data from the dialog
    public interface OnDialogCloseListener {
        void closeDialog(int year, int month, int day, String description, String category);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_adder, container, false);
        toDo = (EditText) view.findViewById(R.id.toDo);
        dp = (DatePicker) view.findViewById(R.id.datePicker);
        add = (Button) view.findViewById(R.id.add);

        //Define and initialize a spinner
        Spinner spinner = (Spinner) view.findViewById(R.id.todo_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout(copied from devloper.android.com)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
        R.array.todo_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears(copied from devloper.android.com)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner(copied from devloper.android.com)
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        dp.updateDate(year, month, day);

        //Added additional varible category to save the category value.
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnDialogCloseListener activity = (OnDialogCloseListener) getActivity();
                activity.closeDialog(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), toDo.getText().toString(), category);
                AddToDoFragment.this.dismiss();
            }
        });

        return view;
    }
}



