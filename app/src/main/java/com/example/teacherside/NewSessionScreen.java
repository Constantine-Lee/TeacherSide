package com.example.teacherside;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class NewSessionScreen extends AppCompatActivity {

    private final static String UNIT_CODE = "UNIT_CODE";

    private Button mButtonDate, mButtonTime, mButtonConfirm;
    private Spinner mSpinnerNumberOfSlots;
    private List<String> mNumbers;
    public static final String EXTRA_SESSION = "session";
    private Calendar calendar = Calendar.getInstance();
    private TimeSlotManager mTimeSlotManager;
    private int mDay, mMonth, mYear;
    private String mUnitCode;

    public static Intent newIntent(Context packageContext, String unitCode){
        Intent intent = new Intent (packageContext, NewSessionScreen.class);
        intent.putExtra(UNIT_CODE, unitCode);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnitCode = getIntent().getStringExtra(UNIT_CODE);
        mNumbers = Arrays.asList("1","2","3","4","5","6","7","8","9","10");
        InitUI();
        LoadResource();
        SetListener();
    }

    private void InitUI(){
        setContentView(R.layout.new_session_screen);
        mButtonDate = (Button) findViewById(R.id.Button_Date);
        mButtonTime = (Button) findViewById(R.id.Button_Time);
        mSpinnerNumberOfSlots = (Spinner) findViewById(R.id.Spinner_AmountOfSlots);
        mButtonConfirm = (Button) findViewById(R.id.BUTTON_CONFIRM);

        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH);
        mYear = calendar.get(Calendar.YEAR);
        mButtonDate.setText(mDay+"/"+(mMonth+1)+"/"+mYear);
    }

    private void LoadResource(){
        mTimeSlotManager = new TimeSlotManager(this, mUnitCode);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,
                mNumbers);
        mSpinnerNumberOfSlots.setAdapter(dataAdapter);
    }

    private void SetListener(){
        SetConfirmButtonListener();
        SetDateButtonListener();
        SetTimeButtonListener();
    }

    private void SetConfirmButtonListener(){
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int stringNumberOfSlots = Integer.parseInt(mSpinnerNumberOfSlots.getSelectedItem().toString());
                mTimeSlotManager.removeAllTimeSlot();
                String stringTime = mButtonTime.getText().toString();
                int mHour = Integer.parseInt(stringTime.substring(0, stringTime.indexOf(":")));
                int mMinutes = Integer.parseInt(stringTime.substring(stringTime.indexOf(":")+1));
                for(int i = 0, hour = mHour, minutes = mMinutes ; i<stringNumberOfSlots; i++, minutes = minutes + 13){
                    if((minutes-60)>=0){
                        hour ++;
                        minutes = minutes%60;
                    }
                    if((hour-24)>=0){
                        hour = hour%24;
                    }
                    DecimalFormat formatter = new DecimalFormat("00");
                    String aFormatString = formatter.format(minutes);
                    TimeSlot tS = new TimeSlot();
                    tS.setTime(hour+" : "+aFormatString+" pm");
                    mTimeSlotManager.setDate(mButtonDate.getText().toString());
                    mTimeSlotManager.addTimeSlot(tS);
                    TimeSlotsInterface.NotifyAdapterDataSetChanged();
                }
                Intent returnIntent = new Intent();

                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void SetDateButtonListener(){
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        datePicker.setMinDate(System.currentTimeMillis()-1000);
                        mButtonDate.setText(day + "/" + month + "/" + year);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        mButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                datePickerDialog.show();
            }
        });
    }

    private void SetTimeButtonListener(){
        final TimePickerDialog timePickerDialog = new TimePickerDialog(this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        mButtonTime.setText(hourOfDay + ":" + minute);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mButtonTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                timePickerDialog.show();
            }
        });
    }
}
