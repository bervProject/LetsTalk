package com.letstalk.letstalk.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.letstalk.letstalk.TextSendListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TalkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TalkFragment extends Fragment {

    // View Handler
    @BindView(R.id.buttonTalk)
    Button button;
    @BindView(R.id.resultTalkBox)
    EditText textResult;
    @BindView(R.id.deleteResultTalkButton)
    ImageButton deleteButton;

    private TextSendListener textSendListener;

    // Bluetooth Section
    private BluetoothAdapter btAdapter;
    private final int START_BLUETOOTH_RC = 10;
    // TODO: FILL MAC ADDRESS OF ARDUINO HERE
    private final String address = "";
    private BluetoothDevice btDevice;
    private BluetoothSocket mmSocket;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private boolean stopWorker;
    private int readBufferPosition;
    private byte[] readBuffer;
    private Thread workerThread;
    private boolean bluetoothOn = false;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof TextSendListener) {
            textSendListener = (TextSendListener) getActivity();
        }
    }

    private void startBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getActivity(), "Not Support Bluetooth. Can't continue this features.", Toast.LENGTH_SHORT).show();
        } else {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, START_BLUETOOTH_RC);
            }
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0 ) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getAddress().equalsIgnoreCase(address)) {
                        btDevice = device;
                        break;
                    }
                }
                if (btDevice != null) {
                    startReceive();
                } else {
                    Toast.makeText(getActivity(),"This Device Never Connected to ARDUINO Device", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(),"Never connected to bluetooth device.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startReceive() {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        try {
            mmSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            changeButtonToStop();
            beginListenForData();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"Can't Create Connection to Arduino", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.buttonTalk)
    void clickStartBluetooth() {
        if (!bluetoothOn) {
            startBluetooth();
        } else {
            stopReceive();
        }
    }

    private void changeButtonToStop() {
        bluetoothOn = true;
        button.setText(R.string.stop);
        button.setBackgroundColor(Color.parseColor("#FF0000"));
    }

    private void changeButtonToStart() {
        bluetoothOn = false;
        button.setText(R.string.button_talk);
        button.setBackgroundColor(getResources().getColor(R.color.buttonColor));
    }

    private void beginListenForData() {
        Toast.makeText(getActivity(),"Listening Data from Arduino", Toast.LENGTH_SHORT).show();

        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            textResult.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopReceive();
    }

    private void stopReceive() {
        stopWorker = true;
        try {
            if (mmOutputStream!= null) {
                mmOutputStream.close();
            }
            if (mmInputStream != null) {
                mmInputStream.close();
            }
            if (mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.talkButton)
    void clickTalk() {
        String text = textResult.getText().toString();
        if (textSendListener != null) {
            textSendListener.callSpeech(text);
        }
    }

    @OnClick(R.id.deleteResultTalkButton)
    void clearText() {
        textResult.getText().clear();
    }

}
