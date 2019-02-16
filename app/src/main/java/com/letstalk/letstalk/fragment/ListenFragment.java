package com.letstalk.letstalk.fragment;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.letstalk.letstalk.R;
import com.letstalk.letstalk.TextSendListener;
import com.letstalk.letstalk.api.SpeechToText;
import com.letstalk.letstalk.model.SpeechToTextResponse;
import com.readystatesoftware.chuck.ChuckInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private SpeechToText serviceSpeechToText;
    private MaterialDialog loading;
    private Call<SpeechToTextResponse> resultTextFromSpeech;

    private final String URL_SERVER = "http://35.240.163.60:5000/";

    Callback<SpeechToTextResponse> callbackResponse = new Callback<SpeechToTextResponse>() {
        @Override
        public void onResponse(@NonNull Call<SpeechToTextResponse> call, @NonNull Response<SpeechToTextResponse> response) {
            if (response.isSuccessful()) {
                SpeechToTextResponse serverResponse = response.body();
                if (serverResponse != null) {
                    String status = serverResponse.getStatus();
                    String result = serverResponse.getResult();
                    if (status != null) {
                        if (status.equalsIgnoreCase("success")) {
                            if (result != null) {
                                resultBox.setText(result);
                            } else {
                                resultBox.setText("");
                                Toast.makeText(getActivity(), "No Result from Server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Failed to Send Data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Different Format Receive from Server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Different Format Receive from Server", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
            }
            loading.dismiss();
        }

        @Override
        public void onFailure(@NonNull Call<SpeechToTextResponse> call, @NonNull Throwable t) {
            Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
            loading.dismiss();
        }
    };

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
        View view = inflater.inflate(R.layout.fragment_listen, container, false);
        ButterKnife.bind(this, view);
        resultBox.setKeyListener(null);
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
            final String resultText = resultBox.getText().toString();

            new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                    .title("Select Language to Speak")
                    .items(R.array.language_selection)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            if (which == 0) {
                                textSendListener.callSpeech("id", resultText,true);
                            } else if (which == 1) {
                                textSendListener.callSpeech("kr", resultText, true);
                            } else {
                                textSendListener.callSpeech("en", resultText, true);
                            }
                            return true;
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showMic();
        prepareRetrofit();
        prepareDialog();
    }

    private void prepareDialog() {
        loading = new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                .title(R.string.sending_data)
                .content(R.string.please_wait_sending_data)
                .progress(true, 0)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(getActivity(), "Cancel Send Data", Toast.LENGTH_SHORT).show();
                        if (resultTextFromSpeech != null && resultTextFromSpeech.isExecuted()) {
                            resultTextFromSpeech.cancel();
                        }
                    }
                })
                .build();
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
            if (serviceSpeechToText != null) {
                loading.show();
                File file = new File(newFilePath);
                RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("audio",
                        file.getName(),
                        requestBody);
                resultTextFromSpeech = serviceSpeechToText.speechToText(body);
                resultTextFromSpeech.enqueue(callbackResponse);
            } else {
                Toast.makeText(getActivity(), "The devices not ready to send files to server.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Please record your voice first please", Toast.LENGTH_SHORT).show();
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
            String nameFile = String.format(Locale.ENGLISH, "new-record-%d.aac", System.currentTimeMillis());
            File newAudio = new File(newPath, nameFile);
            newFilePath = newAudio.getAbsolutePath();
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mRecorder.setOutputFile(newFilePath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            try {
                mRecorder.prepare();
                mRecorder.start();
                showStop();
            } catch (IOException e) {
                String message = "Prepare failed :" + e.getMessage();
                Log.e("Recorder_Lets_Talk", message);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Can't Create Directory", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ChuckInterceptor(Objects.requireNonNull(getActivity()).getApplicationContext()))
                .build();
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(URL_SERVER)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        serviceSpeechToText = retrofit.create(SpeechToText.class);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

}
