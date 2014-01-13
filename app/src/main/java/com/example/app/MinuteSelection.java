package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MinuteSelection extends Activity {
    public static int customerID;
    public static String bedName;
    public static String bedNumber;
    public static int maxTime;
    public static int sessionTime;
    public static CountDownTimer logOutTimer;

    public void startBed(View view) {
        // setup params
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("bed_num", bedNumber));
        params.add(new BasicNameValuePair("time", Integer.toString(sessionTime)));
        params.add(new BasicNameValuePair("cust_num", Integer.toString(customerID)));

        // post to start_bed
        ToastyHttpResponse response = ToastyHTTPHandler.Post("start_bed", params);

        // check for http errors
        if (response.Error != 0) {
            // Display error message
            Context context = getApplicationContext();
            Toast.makeText(context, "Oops, something went wrong\n\nError: " + response.Error, Toast.LENGTH_LONG).show();
            return;
        }

        // Check for errors in JSON response
        try {
            // Parse JSON
            JSONObject jObject = new JSONObject(response.Body);

            // Check for error response
            if (!jObject.isNull("error_code")) {
                /* generic error code for error response from bed_status--shouldn't happen unless
                something really went wrong */
                Context context = getApplicationContext();
                Toast.makeText(context, "Oops, something went wrong\n\nError: 11", Toast.LENGTH_LONG).show();
                return;
            }
        } catch(JSONException e) {
            Context context = getApplicationContext();
            Toast.makeText(context, "Oops, something went wrong\n\nError: 10", Toast.LENGTH_LONG).show();
            return;
        }

        // disable button to prevent double pressing during toast wait
        Button button = (Button) view.findViewById(R.id.minuteButton);
        button.setEnabled(false);

        Context context = getApplicationContext();
        Toast.makeText(context, "Your bed will start in 5 minutes.", Toast.LENGTH_LONG).show();

        /* Go back to login after the toasty is complete to prevent relogging before session is posted to DB.
           This is possible b/c a session can't be logged until the bed is started, and this can take time */
        new CountDownTimer(3000, 3000) {
            public void onTick(long millisUntilFinished) {
                // don't need, do nothing
            }

            public void onFinish() {
                backToLogin();
            }
        }.start();
    }

    public void backToLogin() {
        // goes to the top of the stack so you can't go back here
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

        // create new logOutTimer
        logOutTimer = new CountDownTimer(60000, 60000) {
            public void onTick(long millisUntilFinished) {
                // don't need, do nothing
            }

            public void onFinish() {
                backToLogin();
            }
        };

        // set up dynamic content
        TextView tvBedName = (TextView) findViewById(R.id.minutesBedName);
        tvBedName.setText(bedName);
        TextView tvBedNum = (TextView) findViewById(R.id.minutesBedNumber);
        tvBedNum.setText("Bed " + bedNumber);

        NumberPicker np = (NumberPicker) findViewById(R.id.minutePicker);

        np.setMinValue(0);
        np.setMaxValue(maxTime);
        np.setWrapSelectorWheel(true);
        np.setValue(0);

        // Change time on button, and enable only if minutes > 1
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i2) {
                sessionTime = i2;

                Button button = (Button) findViewById(R.id.minuteButton);
                button.setText("Start " + i2 + " Minute Session");

                if (i2>1) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();

        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        logOutTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        logOutTimer.cancel();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

        logOutTimer.cancel();
        logOutTimer.start();

        // help keep the nav bar in low-profile
        final View view = getWindow().getDecorView();
        if (view.getSystemUiVisibility() != View.SYSTEM_UI_FLAG_LOW_PROFILE){
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
}
