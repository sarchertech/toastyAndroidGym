package com.example.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

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

        //Get ip address, post to /customer_login, parse response, and handle response
        try {
            /* get ip address by hostname
               Doesn't work on the emulator, requires Internet Permissions */
            InetAddress addr = java.net.InetAddress.getByName("SethThinkPadWin");
            String ip = addr.getHostAddress() + ":9000";

            // Setup Post to customer_login
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://" + ip + "/customer_login");
            // Add http timeout
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 1000);

            // Add params
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("Fob_num", keynumString));
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpPost);

            // Check for correct http response code
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus != 200) {
                //TODO log response code - maybe throw an exception
                return 6;
            }

            //Get response body
            ResponseHandler<String> handler = new BasicResponseHandler();
            String httpBody = handler.handleResponse(response);

            //Parse JSON
            JSONObject jObject = new JSONObject(httpBody);

            //Check for error response
            if (!jObject.isNull("error_code")) {
                return jObject.getInt("error_code");
            }

            //No errors encountered - login should be good
            int customerID = jObject.getInt("id");
            String customerName = jObject.getString("name");



            /* Toast for dev purposes */
            Context context = getApplicationContext();
            Toast.makeText(context, customerName, Toast.LENGTH_LONG).show();
            /* end toast */

        } catch (UnknownHostException e) {
            return 7;
        } catch (ClientProtocolException e) {
            return 8;
        } catch (IOException e) {
            return 9; // Most likely ConnectTimeout exception or NoHttpResponse exception caused by server not running
        } catch (JSONException e) {
            e.printStackTrace();
            return 10;
        }

        return 0; // No Error
    }

//    public boolean hide() {
//        View rootView = getWindow().getDecorView();
//        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//
//        Runnable mNavHider = new Runnable() {
//            @Override public void run() {
//                hide();
//            }
//        };
//
//        Handler handler = new Handler();
//        handler.postDelayed(mNavHider, 1000);
//
//        return true;
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
//
//
//    }

    @Override
    protected void onResume() {
        super.onResume();

        View rootView = getWindow().getDecorView();
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

//        Runnable mNavHider = new Runnable() {
//            @Override public void run() {
//                hide();
//            }
//        };
//
//        Handler handler = new Handler();
//        handler.postDelayed(mNavHider, 1000);

        handleLoginErrors(login(9858));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
