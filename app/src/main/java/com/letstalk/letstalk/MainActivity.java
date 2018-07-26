package com.letstalk.letstalk;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Button button;
    private EditText textEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);
        button = findViewById(R.id.button);
        textEdit = findViewById(R.id.editText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOut();
            }
        });
    }

    private void speakOut() {
        String text = textEdit.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH,null);
    }

    @Override
    protected void onDestroy() {
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
            int result = tts.setLanguage(new Locale("id","ID"));

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                Toast.makeText(this,"This Language is not supported", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"Speak Ready", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
            }

        } else {
            Toast.makeText(this,"Initilization Failed!", Toast.LENGTH_SHORT).show();
            Log.e("TTS", "Initilization Failed!");
        }
    }
}
