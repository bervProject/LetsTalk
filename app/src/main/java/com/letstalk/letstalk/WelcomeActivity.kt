package com.letstalk.letstalk

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Window
import android.view.WindowManager

import androidx.appcompat.app.AppCompatActivity

/**
 * WelcomeActivity
 * First page/landing page
 */
class WelcomeActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_welcome)
        timer = object : CountDownTimer(800, 1) {
            override fun onTick(l: Long) {
                Log.i("WelcomeActivity", l.toString());
            }

            override fun onFinish() {
                changeActivity()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (timer != null) {
            timer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (timer != null) {
            timer?.cancel()
        }
    }

    private fun changeActivity() {
        val intent = Intent(this, LetsTalkActivity::class.java)
        startActivity(intent)
        finish()
    }
}
