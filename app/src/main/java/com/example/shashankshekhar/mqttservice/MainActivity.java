package com.example.shashankshekhar.mqttservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    Context context;
    public static SharedPreferences pref;
    public static SharedPreferences prefFile;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences.Editor editorFile;
    public static int dataCount;
    public static int dataCountFile;

    public static Calendar cal;
    public static byte currentSecond;
    public static byte currentHour;
    public static byte currentMinutes;
    public static byte currentDate;
    public static byte currentMonth;
    public static int currentYear_logging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        pref = context.getSharedPreferences("OpenDaySharedPreference", MODE_PRIVATE);
        editor = pref.edit();
        dataCount = pref.getInt("dataCount", 0);

        prefFile = context.getSharedPreferences("OpenDaySharedPreferenceFile", MODE_PRIVATE);
        editorFile = prefFile.edit();
        dataCountFile = prefFile.getInt("dataCountFile", 0);

    }
    public void startService (View view) {

        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        currentDate = (byte) (cal.get(Calendar.DATE));
        currentMonth = (byte) (cal.get(Calendar.MONTH) + 1);
        currentYear_logging = cal.get(Calendar.YEAR);


        Log.e("Date",""+currentDate+currentMonth+currentYear_logging);

        if(currentDate == 4 && currentMonth == 3){
            startService(new Intent(this, PhoneSignalStrengthReaderService.class));
        }
    }
    public void stopService (View view) {
        stopService(new Intent(this, PhoneSignalStrengthReaderService.class));
    }

    public void clearSharedPreference(View view) {

        Log.e("Data Count B", "Value is " + dataCount);
        editor.putInt("dataCount", 0);
        editor.commit();
        dataCount = pref.getInt("dataCount", 0);
        Log.e("Data Count A", "Value is " + dataCount);

        Log.e("Data Count File B", "Value is " + dataCountFile);
        editorFile.putInt("dataCountFile", 0);
        editorFile.commit();
        dataCountFile = prefFile.getInt("dataCountFile", 0);
        Log.e("Data Count File A", "Value is " + dataCountFile);

    }
}
