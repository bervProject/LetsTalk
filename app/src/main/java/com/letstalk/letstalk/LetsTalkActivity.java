package com.letstalk.letstalk;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.letstalk.letstalk.adapter.LetsTalkFragmentAdapter;

import java.util.Locale;

public class LetsTalkActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextSendListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private LetsTalkFragmentAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lets_talk);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new LetsTalkFragmentAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#616870"));

        tts = new TextToSpeech(this, this);
    }

    @Override
    public void callSpeech(String text) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null); // Queue
            // tts.speak(text, TextToSpeech.QUEUE_FLUSH, null); Queue Flush
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            tts.setSpeechRate(0.5f);
            int result = tts.setLanguage(new Locale("id","ID"));
            // int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Speak Ready for all", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Initilization Failed!", Toast.LENGTH_SHORT).show();
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
