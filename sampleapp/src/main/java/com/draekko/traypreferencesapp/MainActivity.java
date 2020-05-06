package com.draekko.traypreferencesapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.draekko.traypreferences.TrayPreference;
import com.draekko.traypreferences.TraySharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static Context staticContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        staticContext = this;

        Set<String> valueSet = new HashSet<>();
        valueSet.add("red");
        valueSet.add("green");
        valueSet.add("blue");
        valueSet.add("white");
        valueSet.add("black");
        valueSet.add("transparent");

        TrayPreference trayPreference = new TrayPreference(staticContext);
        TraySharedPreferences sharedPreferences = trayPreference.getSharedPreferences();
        if (sharedPreferences != null) {
            sharedPreferences.putStringSet("testStringSet", valueSet);
            Set<String> newValueSet = sharedPreferences.getStringSet("testStringSet", null);
            for (String s : newValueSet) {
                Log.v("MainActivity", "color : " + s);
            }
        }

        Button settings = (Button)findViewById(R.id.button_settings);
        if (settings != null) {
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(staticContext, SampleAppPreferences.class);
                    startActivity(intent);
                }
            });
        }
    }
}
