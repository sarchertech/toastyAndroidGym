package com.example.app;

import android.os.Handler;
import android.view.View;

/**
 * Created by learc83 on 1/12/14. Puts navigation bar into low-profile mode. Each time the user
 * touches the action bar it will almost immediately go back into low-profile mode.
 * To use: Make a new Hider, pass it the current view and call hide() from onResume()
 */
public class Hider {
    public View view;

    public Hider(View v) {
        this.view = v;
    }

    public void hide() {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                Runnable mNavHider = new Runnable() {
                    @Override public void run() {
                        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                };

                Handler handler = new Handler();
                // If the delay is set lower, it doesn't work right
                handler.postDelayed(mNavHider, 100);
            }
        });
    }
}
