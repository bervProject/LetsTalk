package com.letstalk.letstalk.fragment;

import android.Manifest;
import android.content.Context;
import android.media.MediaRecorder;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.letstalk.letstalk.R;
import com.letstalk.letstalk.TextSendListener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListenFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.micButton)
    ImageButton micButton;

    @BindView(R.id.stopButton)
    ImageButton stopButton;

    @BindView(R.id.resultListenBox)
    EditText resultBox;

    @BindView(R.id.talkResultButton)
    ImageButton speakButton;

    private int RC_AUDIO_RECORD = 10;
    private MediaRecorder mRecorder;
    private String newFilePath;
    private TextSendListener textSendListener;

    public ListenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListenFragment.
     */
    public static ListenFragment newInstance() {
        return new ListenFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_listen, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @OnClick(R.id.micButton)
    void micClick() {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(Objects.requireNonNull(getActivity()), perms)) {
            recordAudio();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.audio_record),
                    RC_AUDIO_RECORD, perms);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof TextSendListener) {
            textSendListener = (TextSendListener) getActivity();
        }
    }

    @OnClick(R.id.talkResultButton)
    void speak() {
        if (textSendListener != null) {
            String text = resultBox.getText().toString();
            textSendListener.callSpeech(text);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showMic();
    }

    private void showMic() {
        micButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
    }

    private void showStop() {
        stopButton.setVisibility(View.VISIBLE);
        micButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.stopButton)
    void stopRecord() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        showMic();
        sendingFile();
    }

    private void sendingFile() {
        if (newFilePath != null) {
            String message = String.format("Sending %s", newFilePath);
            resultBox.setText(message);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_AUDIO_RECORD) {
            recordAudio();
        }
    }

    private void recordAudio() {
        File cacheDir = Objects.requireNonNull(getActivity()).getCacheDir();
        File newPath = new File(cacheDir, "letstalk");
        boolean success = true;
        if (!newPath.exists()) {
            success = newPath.mkdir();
        }
        if (success) {
            String nameFile = String.format(Locale.ENGLISH,"new-record-%d.m4a",System.currentTimeMillis());
            File newAudio = new File(newPath, nameFile);
            newFilePath = newAudio.getAbsolutePath();
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(newFilePath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                String message = "Prepare failed :" + e.getMessage();
                Log.e("Recorder_Lets_Talk", message);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
            mRecorder.start();
            showStop();
        } else {
            Toast.makeText(getActivity(), "Can't Create Directory", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

}
