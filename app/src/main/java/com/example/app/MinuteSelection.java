package com.example.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MinuteSelection extends Activity {
    public static int customerID;
    public static String bedName;
    public static String bedNumber;
    public static int maxTime;
    public static int sessionTime;

    public void startBed(View view) {
        // setup params
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("bed_num", bedNumber));
        params.add(new BasicNameValuePair("time", Integer.toString(sessionTime)));
        params.add(new BasicNameValuePair("cust_num", Integer.toString(customerID)));

        // post to start_bed
        ToastyHttpResponse response = ToastyHTTPHandler.Post("start_bed", params);

        if (response.Error != 0) {
            // Display error message
            Context context = getApplicationContext();
            Toast.makeText(context, "Oops, something went wrong\n\nError: " + response.Error, Toast.LENGTH_LONG).show();
            return;
        }

        // Check for errors in JSON response TODO

        // Create new intent and go back to login activity (MainActivity)
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Makes login top of the stack so you can't go back here
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minute_selection);

        // get information from calling activity
        Intent intent = getIntent();
        customerID = intent.getIntExtra(MainActivity.CUSTOMER_ID, 0);
        bedName = intent.getStringExtra(BedSelection.BED_NAME);
        bedNumber = intent.getStringExtra(BedSelection.BED_NUM);
        maxTime = intent.getIntExtra(BedSelection.MAX_TIME, 0);

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        // set up dynamic content
        TextView tvBedName = (TextView) findViewById(R.id.minutesBedName);
        tvBedName.setText(bedName);
        TextView tvBedNum = (TextView) findViewById(R.id.minutesBedNumber);
        tvBedNum.setText("Bed " + bedNumber);

        NumberPicker np = (NumberPicker) findViewById(R.id.minutePicker);
//        String[] nums = new String[20];
//        for(int i=0; i<nums.length; i++)
//            nums[i] = Integer.toString(i);

        np.setMinValue(0);
        np.setMaxValue(maxTime);
        np.setWrapSelectorWheel(true);
        np.setValue(0);

        // Change time on button, and enable only if minutes > 1
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                sessionTime = i2;

                Button button = (Button) findViewById(R.id.minuteButton);;
                button.setText("Start " + i2 + " Minute Session");

                if (i2>1) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
        });

        //TextView tv = (TextView) np.getChildAt(0);
        //tv.setTextColor(Color.parseColor("#FFFFFF"));

        //np.setBackgroundColor(Color.parseColor("#FFFFFF"));


        /* Toast for dev purposes */
        //Context context = getApplicationContext();
        //Toast.makeText(context, bedName + customerID, Toast.LENGTH_SHORT).show();
        /* end toast */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.minute_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
