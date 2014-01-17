package com.example.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    // key definition for communication through intent messages
    public final static String CUSTOMER_LEVEL = "com.example.app.CUSTOMER_LEVEL";
    public final static String CUSTOMER_ID = "com.example.app.CUSTOMER_ID";
    public final static String CUSTOMER_NAME = "com.example.app.CUSTOMER_NAME";
    public final static String GET_IP = "com.example.app.GET_IP";

    // for shared preferences
    public final static String IP = "com.example.app.IP";

    //public static String IP = "";
    public static ProgressDialog lostConnectionDialog;

    public void handleLoginErrors(int errCode) {
        String errMessage = "Oops, something went wrong.\n\nPlease try again or contact an employee." +
                "\n\nError Code: " + errCode; // default error message

        // handle the non default error codes and no error code
        switch (errCode) {
            case 0: return; // No error, exit method
            case 2: errMessage = "Sorry :(  We don't recognize that keyfob. \n\nPlease contact an employee";
                    break;
            case 3: errMessage = "Sorry :(  Your account isn't authorized. \n\nPlease contact an employee.";
                    break;
            case 4: errMessage = "It looks like you've already tanned today.";
                    break;
        }

        // Display error message
        Context context = getApplicationContext();
        Toast.makeText(context, errMessage, Toast.LENGTH_LONG).show();
    }

    /* will return 0 if successful, otherwise returns an error code
       error codes 1-4 come from the server, for explanation of error codes 6-10 read the code
       for the method below (login)
    */
    public int login(long keynum) {
        String keynumString = Long.toString(keynum);

        // POST to customer_login and handle response
        try {
            // create parameters
            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("Fob_num", keynumString));

            // post to customer_login
            ToastyHTTPHandler thandler = new ToastyHTTPHandler(this);
            ToastyHttpResponse response = thandler.post("customer_login", params);

            if (response.Error != 0) {
                return response.Error;
            }

            // Parse JSON
            JSONObject jObject = new JSONObject(response.Body);

            // Check for error response
            if (!jObject.isNull("error_code")) {
                return jObject.getInt("error_code");
            }

            // No errors encountered - login should be good
            int customerID = jObject.getInt("id");
            int customerLevel = jObject.getInt("level");
            String customerName = jObject.getString("name");

            // Successful login, start new activity for bed selection and pass relevant messages
            Intent intent = new Intent(this, BedSelection.class);
            intent.putExtra(CUSTOMER_ID, customerID);
            intent.putExtra(CUSTOMER_LEVEL, customerLevel);
            intent.putExtra(CUSTOMER_NAME, customerName);
            startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
            return 10;
        }

        return 0; // No Error
    }

    @Override
    protected void onResume() {
        super.onResume();

        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    @Override
    public void onUserInteraction() {
        // help keep the nav bar in low-profile
        final View view = getWindow().getDecorView();
        if (view.getSystemUiVisibility() != View.SYSTEM_UI_FLAG_LOW_PROFILE){
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        //handleLoginErrors(login(9858));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check to see if we are supposed to scan for an IP--if another activity said so, or IP is blank
        Boolean getID = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) { // have to check b/c getExtras may return null
            getID = extras.getBoolean(GET_IP, false);
        }
        // IP is stored in shared preferences so that it's persistent
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (getID || prefs.getString(IP, "").isEmpty()) {
            IPFinder task = new IPFinder(this);
            task.execute();
        }

         /* Handle hidden keyfob text field
           Note: hardware keyboards always return actionId=0 when pressing enter, ignoring imeOptions="actionSend" in the xml
           layout. Also we have to check for the downpress in addition to the enter key or it will fire twice--on down and up
         */
        EditText editText = (EditText) findViewById(R.id.keyfob_entry);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && event.getAction() == KeyEvent.ACTION_DOWN) {
                    long keynum = Long.parseLong(v.getText().toString());

                    v.setText(""); // clear keyfob text field after getting keyfob number

                    handleLoginErrors(login(keynum));

                    return true;
                }
                return false;
            }
        });
        /* End handle keyfob field */
    }
}

class IPFinder extends AsyncTask<Void, Void, String>
{
    private ProgressDialog dialog;
    private Activity activity;


    public IPFinder(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute()
    {
        this.dialog = ProgressDialog.show(activity, "Lost Connection", "Please wait while Toasty reconnects.\n\nThis may take up to a minute.", true);
        View view = dialog.getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    @Override
    protected String doInBackground(Void... params)
    {
        ToastyHTTPHandler thandler = new ToastyHTTPHandler(activity);
        thandler.scanSubnet();
        return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        this.dialog.dismiss();
        View view = activity.getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }
}
