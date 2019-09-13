package com.letstalk.letstalk

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import com.letstalk.letstalk.adapter.LetsTalkFragmentAdapter
import com.letstalk.letstalk.fragment.TalkFragment
import java.util.*

/**
 * LetsTalkActivity
 * Main activity of this apps
 */
class LetsTalkActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TextSendListener {

    /**
     * The [PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: LetsTalkFragmentAdapter? = null
    /**
     * The [ViewPager] that will host the section contents.
     */
    @BindView(R.id.container)
    @JvmField
    var mViewPager: ViewPager? = null
    @BindView(R.id.tabs)
    @JvmField
    var tabLayout: TabLayout? = null

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lets_talk)
        ButterKnife.bind(this)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = LetsTalkFragmentAdapter(supportFragmentManager)
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout!!.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(mViewPager))
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                if (i == 0) {
                    val talkFragment = mSectionsPagerAdapter!!.getItem(i + 1) as TalkFragment
                    talkFragment.stopReceive()
                }
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
        tabLayout!!.setTabTextColors(Color.parseColor("#FFFFFF"), Color.parseColor("#616870"))

        tts = TextToSpeech(this, this)
    }

    override fun callSpeech(text: String) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // Default English
            tts!!.language = Locale.ENGLISH
            tts!!.speak(text, TextToSpeech.QUEUE_ADD, null, null) // Queue
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show()
        }
    }

    override fun callSpeech(text: String, flushMode: Boolean) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // Default English
            tts!!.language = Locale.ENGLISH
            if (flushMode) {
                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                tts!!.speak(text, TextToSpeech.QUEUE_ADD, null, null)
            }
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show()
        }
    }

    override fun callSpeech(language: String, text: String, flushMode: Boolean) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            val mode: Int = if (flushMode) {
                TextToSpeech.QUEUE_FLUSH
            } else {
                TextToSpeech.QUEUE_ADD
            }
            val locale: Locale = when {
                language.equals("id", ignoreCase = true) -> Locale("id", "ID")
                language.equals("kr", ignoreCase = true) -> Locale.KOREAN
                else -> Locale.ENGLISH
            }
            val result = tts!!.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show()
            } else {
                tts!!.speak(text, mode, null, null)
            }
        } else {
            Toast.makeText(this, "Please turn off silenced/vibrate mode. Can't play the audio.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onInit(i: Int) {
        if (i == TextToSpeech.SUCCESS) {
            tts!!.setSpeechRate(0.5f)
            val result = tts!!.setLanguage(Locale("id", "ID"))
            // int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
                Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Speak Ready for all", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "Initilization Failed!", Toast.LENGTH_SHORT).show()
            Log.e("TTS", "Initilization Failed!")
        }
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}
