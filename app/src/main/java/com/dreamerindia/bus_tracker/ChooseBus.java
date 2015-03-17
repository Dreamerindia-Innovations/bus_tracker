package com.dreamerindia.bus_tracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by user on 12-03-2015.
 */
public class ChooseBus extends Activity {
    EditText bno;
    long lastPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_bus);
        bno = (EditText) findViewById(R.id.busNo);
    }

    public void search(View v) {
        String temp = bno.getText().toString();

        if (temp.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter the Bus Number", Toast.LENGTH_SHORT).show();
        } else {
            int bb = Integer.parseInt(temp);
            if (bb == 01) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), "The Bus Number you have entered is not available", Toast.LENGTH_SHORT).show();
            }

        }
    }
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastPress > 5000){
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_LONG).show();
            lastPress = currentTime;
        }else{
            super.onBackPressed();

        }
    }
}
