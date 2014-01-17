package com.example.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.loopj.android.http.*;

/**
 * Created by learc83 on 1/8/14. Handles all http communication
 * returns a ToastyHTTPResponse object with an error and response field
 */
public class ToastyHTTPHandler {
    private Activity activity;

    public ToastyHTTPHandler(Activity activity) {
        this.activity = activity;
    }

    public ToastyHttpResponse post(String endpoint, List<NameValuePair> params) { //add nested param to trap stack depth
        //Get ip address and post params to supplied endpoint
        try {
            /* get ip address by hostname
               Doesn't work on the emulator, requires Internet Permissions */

            // get IP from shared prefs
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String ip = prefs.getString(MainActivity.IP, "");

            // Setup Post to customer_login
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://" + ip + ":9000/" + endpoint);
            // Add http timeout
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 1000);

            // Add params
            //List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            //params.add(new BasicNameValuePair("Fob_num", keynumString));
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpPost);

            // Check for correct http response code
            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus != 200) {
                //TODO log response code - maybe throw an exception
                return new ToastyHttpResponse(6);
            }

            // Get response body
            ResponseHandler<String> handler = new BasicResponseHandler();
            String httpBody = handler.handleResponse(response);

            return new ToastyHttpResponse(0, httpBody);

        } catch (UnknownHostException e) {
            return new ToastyHttpResponse(7);
        } catch (ClientProtocolException e) {
            return new ToastyHttpResponse(8);
        } catch (IOException e) {
            backToLoginToGetIP();
            return new ToastyHttpResponse(9); // Most likely ConnectTimeout exception or NoHttpResponse exception caused by server not running
        }
    }

    //Search all 256 IP addresses on this subnet, TODO will return on any 200 status on :9000--check toasty specific response WARNING
    public void scanSubnet() {

        //String ip = "192.168.1.114";
        byte[] addrBytes;

        // TODO WARNING this may crash if the device doesn't have an IP
        try {
            String ip = getLocalIpAddress();

            InetAddress addr = InetAddress.getByName(ip);
            addrBytes = addr.getAddress();

        } catch (UnknownHostException e) {
            e.printStackTrace();//TODO handle exception
            return;
        }

        for (int i=0; i<256; i++) {
            addrBytes[3] = (byte) i;

            try {
                // first make sure the address is on the network
                String ipString = InetAddress.getByAddress(addrBytes).getHostAddress();

                // Setup Post to customer_login
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://" + ipString + ":9000/customer_login");
                // Add http timeout
                HttpParams httpParams = httpclient.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 200);
                HttpConnectionParams.setSoTimeout(httpParams, 200);

                // Execute HTTP Post Request TODO change to GET
                HttpResponse response = httpclient.execute(httpPost);

                // Check for correct http response code
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus == 200) {
                    //Set ip
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                    prefs.edit().putString(MainActivity.IP, ipString).commit();
                    return;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();//TODO handle exception
            } catch (ClientProtocolException e) {
                e.printStackTrace();//TODO handle exception
            } catch (IOException e) {
                e.printStackTrace();//TODO handle exception
            }
        }
        return;
    }

    // go back to login, set GET_IP so login activity knows to scan the subnet for toasty box IP
    private void backToLoginToGetIP() {
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.GET_IP, true);
        activity.startActivity(intent);
    }

    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    // check to make sure IP is not 127... or 0.0.... and that it is IPv4 not 6
                    if (!inetAddress.isLoopbackAddress()  && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}

// I hate JAVA so much. Creating an object just so I could return an optional error code
class ToastyHttpResponse {
    public Integer Error;
    public String Body;

    public ToastyHttpResponse(int err, String body) {
        this.Error = err;
        this.Body = body;
    }

    public ToastyHttpResponse(int err) {
        this.Error = err;
    }
}
