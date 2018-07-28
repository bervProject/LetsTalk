package com.letstalk.letstalk.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.letstalk.letstalk.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TalkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TalkFragment extends Fragment implements TextToSpeech.OnInitListener {

    @BindView(R.id.buttonTalk)
    Button talkButton;
    @BindView(R.id.resultTalkBox)
    EditText textResult;
    @BindView(R.id.deleteResultTalkButton)
    ImageButton deleteButton;

    private TextToSpeech tts;

   public TalkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TalkFragment.
     */
    public static TalkFragment newInstance() {
        return new TalkFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_talk, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(getActivity(), this);
    }

    @OnClick(R.id.buttonTalk)
    void clickTalk() {
        speakOut();
    }

    @OnClick(R.id.deleteResultTalkButton)
    void clearText() {
        textResult.getText().clear();
    }

    private void speakOut() {
        String text = textResult.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            tts.setSpeechRate(0.5f);
            // int result = tts.setLanguage(new Locale("id","ID"));
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                Toast.makeText(getActivity(),"This Language is not supported", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),"Speak Ready", Toast.LENGTH_SHORT).show();
                talkButton.setEnabled(true);
            }

        } else {
            Toast.makeText(getActivity(),"Initilization Failed!", Toast.LENGTH_SHORT).show();
            Log.e("TTS", "Initilization Failed!");
        }
    }
}
