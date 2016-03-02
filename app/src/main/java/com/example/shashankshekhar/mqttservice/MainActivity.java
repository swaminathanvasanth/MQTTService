package com.example.shashankshekhar.mqttservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Context context;
    public static SharedPreferences pref;
    public static SharedPreferences prefFile;
    public static SharedPreferences.Editor editor;
    public static SharedPreferences.Editor editorFile;
    public static int dataCount;
    public static int dataCountFile;



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
        startService(new Intent(this, PhoneSignalStrengthReaderService.class));
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
