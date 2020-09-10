package com.rohit.examples.android.thepigeonletters.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.rohit.examples.android.thepigeonletters.R;

public class SplashActivity extends AppCompatActivity {

    // Variable declaration for available views on UI
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Fetching IDs of views from ID resource
        linearLayout = findViewById(R.id.linearlayout);

        //Hiding the views
        linearLayout.setVisibility(View.INVISIBLE);
        // Constant time variables to handle animation delays
        int START_TIME = 1500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation fade = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
                linearLayout.startAnimation(fade);
                linearLayout.setVisibility(View.VISIBLE);
            }
        }, START_TIME);

        // Handling time delay for launching next activity
        int LAUNCH_TIME = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intentMain = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intentMain);

                finish();

            }
        }, LAUNCH_TIME);
    }
}