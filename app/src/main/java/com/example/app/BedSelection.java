package com.example.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BedSelection extends Activity implements ActionBar.TabListener {
    public final static String BED_NAME = "com.example.app.BED_NAME";
    public final static String BED_NUM = "com.example.app.BED_NUM";
    public final static String MAX_TIME = "com.example.app.MAX_TIME";

    // global so they are easily available to fragments
    public static int customerID;
    public static int customerLevel;
    public static String customerName;
    public static BedsAdapter adapter;
    public static ArrayList<Bed> beds;
    public static CountDownTimer logOutTimer;

    public int getBedsByLevel(int lvl) {
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("Level", Integer.toString(lvl)));

        // post to customer_login
        ToastyHttpResponse response = ToastyHTTPHandler.Post("bed_status", params);

        if (response.Error != 0) {
            return response.Error;
        }

        try {
            // Parse JSON
            JSONObject jObject = new JSONObject(response.Body);

            // Check for error response
            if (!jObject.isNull("error_code")) {
                /* generic error code for error response from bed_status--shouldn't happen unless
                something really went wrong */
                return 11;
            }

            JSONArray array = jObject.getJSONArray("beds");

            // Convert JSON array to array of Bed objects
            beds = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);

                String name = row.getString("Name");
                String num = row.getString("Bed_num");
                int maxTime = row.getInt("Max_time");
                Boolean status = row.getBoolean("Status");

                beds.add(new Bed(name, num, maxTime, status));
            }

            adapter.clear();
            adapter.addAll(beds);

        } catch (JSONException e) {
            e.printStackTrace();
            /* TODO This is a hack to clear the beds even if no beds are returned
               Fix this by checking for null beds and handling it there */
            adapter.clear();
            return 10;
        }

        return 0; // No Error
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bed_selection);

        Intent intent = getIntent();
        customerID = intent.getIntExtra(MainActivity.CUSTOMER_ID, 0);
        customerLevel=  intent.getIntExtra(MainActivity.CUSTOMER_LEVEL, 0);
        customerName =  intent.getStringExtra(MainActivity.CUSTOMER_NAME);

        // create new logOutTimer
        logOutTimer = new CountDownTimer(60000, 60000) {
            public void onTick(long millisUntilFinished) {
                // don't need, do nothing
            }

            public void onFinish() {
                finish();
            }
        };

        /* Create new adapter to handle populating beds, and attach to ListView
           This has to come before setting up the tabs, so that an adapter exists when a tab is selected */
        adapter = new BedsAdapter(this);
        GridView gridView = (GridView) findViewById(R.id.bedList);
        gridView.setAdapter(adapter);

        // Set up tabs for each level the customer has access to except highest one
        ActionBar bar = getActionBar();
        for (int i=1; i<customerLevel; i++) {
            bar.addTab(bar.newTab().setText("Level " + i).setTabListener(this), i-1, false);
        }

        // Set up tab for the highest level customer has access to and select it by default
        bar.addTab(bar.newTab().setText("Level " + customerLevel).setTabListener(this), customerLevel-1, true);

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Add listener to handle selecting beds
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Bed bed = beds.get(position);

                Intent intent = new Intent(getApplicationContext(), MinuteSelection.class);
                intent.putExtra(MainActivity.CUSTOMER_ID, customerID);
                intent.putExtra(BED_NAME, bed.Name);
                intent.putExtra(BED_NUM, bed.Number);
                intent.putExtra(MAX_TIME, bed.MaxTime);
                startActivity(intent);
            }
        });
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        int t = tab.getPosition();

        getBedsByLevel(t + 1); //TODO handle errors
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // Do Nothing
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // Do Nothing
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

class Bed {
    public String Name;
    public String Number;
    public int MaxTime;
    public Boolean Status;

    public Bed(String name, String number, int maxTime, Boolean status) {
        this.Name = name;
        this.Number = number;
        this.MaxTime = maxTime;
        this.Status = status;
    }
}
