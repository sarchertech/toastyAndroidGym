package com.example.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;

public class BedSelection extends Activity implements ActionBar.TabListener {
    // global so they are easily available to fragments
    public static int customerID;
    public static int customerLevel;
    public static String customerName;

    public void getBedsByLevel(int lvl) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bed_selection);

        Intent intent = getIntent();
        customerID = intent.getIntExtra(MainActivity.CUSTOMER_ID, 0);
        customerLevel=  5;// TODO remove comments intent.getIntExtra(MainActivity.CUSTOMER_LEVEL, 0);
        customerName =  intent.getStringExtra(MainActivity.CUSTOMER_NAME);

        ActionBar bar = getActionBar();

        // Set up tabs for each level the customer has access to except highest one
        for (int i=1; i<customerLevel; i++) {
            bar.addTab(bar.newTab().setText("Level " + i).setTabListener(this), i-1, false);
        }

        // Set up tab for the highest level customer has access to and select it by default
        bar.addTab(bar.newTab().setText("Level " + customerLevel).setTabListener(this), customerLevel-1, true);

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        /* Toast for dev purposes */
        Context context = getApplicationContext();
        Toast.makeText(context, customerName, Toast.LENGTH_LONG).show();
        /* end toast */
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        int t = tab.getPosition();

        /* Toast for dev purposes */
        Context context = getApplicationContext();
        Toast.makeText(context, "tab " + (t+1), Toast.LENGTH_SHORT).show();
        /* end toast */
        // Do stuff based on new tab selected

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

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.bed_selection, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
