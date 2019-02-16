package com.letstalk.letstalk;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.letstalk.letstalk.adapter.LetsTalkFragmentAdapter;
import com.letstalk.letstalk.fragment.TalkFragment;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class LetsTalkActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextSendListener {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
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

        final TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    TalkFragment talkFragment = (TalkFragment) mSectionsPagerAdapter.getItem(i+1);
                    if (talkFragment != null) {
                        talkFragment.stopReceive();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#616870"));

        tts = new TextToSpeech(this, this);
    }

    @Override
    public void callSpeech(String text) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            // Default English
            tts.setLanguage(Locale.ENGLISH);
            tts.speak(text, TextToSpeech.QUEUE_ADD, null); // Queue
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void callSpeech(String text, boolean flushMode) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            // Default English
            tts.setLanguage(Locale.ENGLISH);
            if (flushMode) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null); // Queue Flush
            } else {

                tts.speak(text, TextToSpeech.QUEUE_ADD, null); // Queue
            }
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void callSpeech(String language, String text, boolean flushMode) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            int mode;
            if (flushMode) {
                mode = TextToSpeech.QUEUE_FLUSH;
            } else {
                mode = TextToSpeech.QUEUE_ADD;
            }
            Locale locale;
            if (language.equalsIgnoreCase("id")) {
                locale = new Locale("id", "ID");
            } else if (language.equalsIgnoreCase("kr")) {
                locale = Locale.KOREAN;
            } else {
                locale = Locale.ENGLISH;
            }
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show();
            } else {
                tts.speak(text, mode, null);
            }
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            tts.setSpeechRate(0.5f);
            int result = tts.setLanguage(new Locale("id", "ID"));
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
