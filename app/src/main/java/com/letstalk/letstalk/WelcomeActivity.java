package com.letstalk.letstalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        timer = new CountDownTimer(800,1) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                changeActivity();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null)
            timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer!= null)
            timer.cancel();
    }

    private void changeActivity() {
        Intent intent = new Intent(this, LetsTalkActivity.class);
        startActivity(intent);
        finish();
    }
}
