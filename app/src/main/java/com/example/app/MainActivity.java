package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
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
            ToastyHttpResponse response = ToastyHTTPHandler.Post("customer_login", params);

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

        // setup navigation bar hiding
        View rootView = getWindow().getDecorView();
        Hider hider = new Hider(rootView);
        hider.hide();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

//        View decorView = getWindow().getDecorView();
//        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
    }
}
