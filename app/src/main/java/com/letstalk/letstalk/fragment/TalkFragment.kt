package com.letstalk.letstalk.fragment

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.letstalk.letstalk.R
import com.letstalk.letstalk.TextSendListener

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import dmax.dialog.SpotsDialog

/**
 * A simple [Fragment] subclass.
 * Use the [TalkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TalkFragment : Fragment() {

    private var textSendListener: TextSendListener? = null

    // Bluetooth Section
    private var btAdapter: BluetoothAdapter? = null
    private val START_BLUETOOTH_RC = 10
    // TODO: FILL MAC ADDRESS OF ARDUINO HERE
    private val address = ""
    private var btDevice: BluetoothDevice? = null
    private var mmSocket: BluetoothSocket? = null
    private var mmOutputStream: OutputStream? = null
    private var mmInputStream: InputStream? = null
    private var stopWorker: Boolean = false
    private var readBuffer: ByteArray? = null
    private var bluetoothOn = false
    private var readBufferPosition: Int = 0
    private var selectedLanguage = "en"

    private var waitDialog: AlertDialog? = null
    private val newDevices = ArrayList<BluetoothDevice>()
    // View Handler
    @BindView(R.id.buttonTalk)
    @JvmField
    var button: Button? = null

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                //discovery starts, we can show progress dialog or perform other tasks
                waitDialog!!.show()
                newDevices.clear()
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                //discovery finishes, dismiss progress dialog
                waitDialog!!.dismiss()
                showNewDevice()
            } else if (BluetoothDevice.ACTION_FOUND == action) {
                //bluetooth device found
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                newDevices.add(device)
                Toast.makeText(context, "Find: " + device!!.name + ", " + device.address, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @BindView(R.id.resultTalkBox)
    @JvmField
    var textResult: EditText? = null
    @BindView(R.id.deleteResultTalkButton)
    @JvmField
    var deleteButton: ImageButton? = null

    private val listOfWords = ArrayList<Map<String, String>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_talk, container, false)
        ButterKnife.bind(this, view)
        textResult!!.keyListener = null
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWords()
    }

    private fun initWords() {
        listOfWords.clear()
        val sorry = HashMap<String, String>()
        sorry["id"] = "Maafkan aku"
        sorry["en"] = "I'm sorry"
        sorry["kr"] = "미안 해요"
        val goodLuck = HashMap<String, String>()
        goodLuck["id"] = "Semoga Beruntung"
        goodLuck["en"] = "Good Luck!"
        goodLuck["kr"] = "행운을 빕니다"
        val hello = HashMap<String, String>()
        hello["id"] = "Halo"
        hello["en"] = "Hello"
        hello["kr"] = "안녕하세요"
        val goodBye = HashMap<String, String>()
        goodBye["id"] = "Selamat Tinggal"
        goodBye["en"] = "Good Bye"
        goodBye["kr"] = "안녕"
        // Must in order when add, not in Initialization
        listOfWords.add(sorry) // 0
        listOfWords.add(goodLuck) // 1
        listOfWords.add(hello) // 2
        listOfWords.add(goodBye) // 3
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is TextSendListener) {
            textSendListener = activity as TextSendListener?
        }
        waitDialog = SpotsDialog
                .Builder()
                .setContext(this.requireContext())
                .build()
        /*
                MaterialDialog.Builder(this.context!!)
                .title(R.string.please_wait)
                .content(R.string.finding_new_device)
                .onAny { dialog, which ->
                    if (btAdapter != null) {
                        if (btAdapter!!.isDiscovering) {
                            btAdapter!!.cancelDiscovery()
                        }
                    }
                }
                .progress(true, 0)
                .build()*/
        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        this.activity!!.registerReceiver(mReceiver, filter)
    }

    private fun startBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter == null) {
            Toast.makeText(activity, "Not Support Bluetooth. Can't continue this features.", Toast.LENGTH_SHORT).show()
        } else {
            if (!btAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, START_BLUETOOTH_RC)
            } else {
                showMethod()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == START_BLUETOOTH_RC) {
            if (resultCode == Activity.RESULT_OK) {
                showMethod()
            } else {
                Toast.makeText(activity, "Please turn on bluetooth", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showMethod() {
        val selectMethodDialog = MaterialDialog(this.requireContext()).listItemsSingleChoice(R.array.method_selection) { _, index, _ ->
            if (index == 0) {
                // Paired Device
                showPairedDevice()
            } else {
                // Find New Device
                findNewDevice()
            }
        }

        MaterialDialog(this.requireContext()).show {
            listItemsSingleChoice(R.array.language_selection) { _, index, _ ->
                selectedLanguage = when (index) {
                    0 -> "id"
                    1 -> "kr"
                    else -> "en"
                }
                selectMethodDialog.show()
            }
        }
    }

    private fun findNewDevice() {
        if (btAdapter!!.isDiscovering) {
            // Bluetooth is already in mode discovery mode, we cancel to restart it again
            btAdapter!!.cancelDiscovery()
        }
        btAdapter!!.startDiscovery()
    }

    private fun showNewDevice() {
        if (newDevices.size > 0) {
            val newDevicesName = ArrayList<String>()
            for (device in newDevices) {
                newDevicesName.add(device.name)
            }
            MaterialDialog(this.requireContext())
                    .show {
                        listItemsSingleChoice(items = newDevicesName) { _, index, _ ->
                            if (index >= newDevices.size) {
                                Toast.makeText(activity, "False Reaction", Toast.LENGTH_SHORT).show()
                            } else {
                                btDevice = newDevices[index]
                                startReceive()
                            }
                        }
                    }
        } else {
            Toast.makeText(activity, "No Devices Found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPairedDevice() {
        val pairedDevices = btAdapter!!.bondedDevices
        val devicesName = ArrayList<String>()
        val devices = ArrayList<BluetoothDevice>()
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                devicesName.add(device.name)
                devices.add(device)
            }
            MaterialDialog(this.requireContext()).show {
                listItemsSingleChoice(items = devicesName) { _, index, _ ->
                    if (index >= pairedDevices.size) {
                        Toast.makeText(activity, "False Reaction", Toast.LENGTH_SHORT).show()
                    } else {
                        btDevice = devices[index]
                        startReceive()
                    }
                }
            }
        } else {
            Toast.makeText(activity, "Never connected to bluetooth device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startReceive() {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") //Standard SerialPortService ID
        try {
            mmSocket = btDevice!!.createRfcommSocketToServiceRecord(uuid)
            mmSocket!!.connect()
            mmOutputStream = mmSocket!!.outputStream
            mmInputStream = mmSocket!!.inputStream
            changeButtonToStop()
            beginListenForData()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, "Can't Create Connection to this device", Toast.LENGTH_SHORT).show()
        }

    }

    @OnClick(R.id.buttonTalk)
    internal fun clickStartBluetooth() {
        if (!bluetoothOn) {
            startBluetooth()
        } else {
            stopReceive()
        }
    }

    private fun changeButtonToStop() {
        bluetoothOn = true
        if (button != null) {
            button!!.setText(R.string.stop)
            button!!.setBackgroundColor(Color.parseColor("#FF0000"))
        }
    }

    private fun changeButtonToStart() {
        bluetoothOn = false
        if (button != null) {
            button!!.setText(R.string.button_talk)
            button!!.setBackgroundColor(resources.getColor(R.color.buttonColor))
        }
    }

    private fun beginListenForData() {
        Toast.makeText(activity, "Listening Data from Arduino", Toast.LENGTH_SHORT).show()
        val delimiter: Byte = 10
        stopWorker = false
        readBuffer = ByteArray(1024)
        readBufferPosition = 0
        val handler = Handler()
        val workerThread = Thread(Runnable {
            val previousWord = StringBuilder()
            var lastWordTime: Long = 0
            while (!Thread.currentThread().isInterrupted && !stopWorker) {
                try {
                    val bytesAvailable = mmInputStream!!.available()
                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
                        mmInputStream!!.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val b = packetBytes[i]
                            if (b == delimiter) {
                                val encodedBytes = ByteArray(readBufferPosition)
                                System.arraycopy(readBuffer!!, 0, encodedBytes, 0, encodedBytes.size)
                                val data = String(encodedBytes, StandardCharsets.US_ASCII)
                                readBufferPosition = 0

                                val convertedText = handleData(data)
                                if (convertedText != null) {
                                    if (!previousWord.toString().equals(convertedText, ignoreCase = true)) {
                                        previousWord.setLength(0)
                                        previousWord.append(convertedText)
                                        lastWordTime = System.currentTimeMillis()
                                        handler.post { speak(selectedLanguage, convertedText, false) }
                                    } else {
                                        val currentTime = System.currentTimeMillis()
                                        val duration = currentTime - lastWordTime
                                        if (duration >= 3000) { // 3000 ms = 3 s
                                            previousWord.setLength(0)
                                            previousWord.append(convertedText)
                                            lastWordTime = System.currentTimeMillis()
                                            handler.post { speak(selectedLanguage, convertedText, false) }
                                        }
                                    }
                                } else {
                                    handler.post {
                                        Toast.makeText(activity, "Unknown Input Format", Toast.LENGTH_SHORT).show()
                                        // For debug only, after finish, commend the line below
                                        Toast.makeText(activity, displayCharValues(data), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                readBufferPosition = (readBufferPosition + 1) % 1024
                                readBuffer!![readBufferPosition] = b
                            }
                        }
                    }
                } catch (ex: IOException) {
                    stopWorker = true
                }

            }
        })

        workerThread.start()
    }

    private fun displayCharValues(s: String): String {
        val sb = StringBuilder()
        for (c in s.toCharArray()) {
            sb.append(c.toInt()).append(",")
        }
        return sb.toString()
    }

    private fun stripNonDigits(input: CharSequence): String {
        val sb = StringBuilder(input.length)
        for (element in input) {
            if (element.toInt() in 48..57) {
                sb.append(element)
            }
        }
        return sb.toString()
    }

    private fun handleData(data: String): String? {
        return try {
            val value = Integer.parseInt(stripNonDigits(data))
            if (value in 1..4) {
                listOfWords[value - 1][selectedLanguage]
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }

    }

    private fun speak(lang: String, text: String, mode: Boolean) {
        var text = text
        if (!text.contains("\n")) {
            text = text + "\n"
        }
        textResult!!.append(text) // append to fill box without erase another text before, change to setText if only want one word/sentence
        if (textSendListener != null) {
            textSendListener!!.callSpeech(lang, text, mode)
        }
    }

    override fun onStop() {
        super.onStop()
        stopReceive()
    }

    override fun onDestroyView() {
        this.activity!!.unregisterReceiver(mReceiver)
        super.onDestroyView()
    }

    fun stopReceive() {
        stopWorker = true
        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.close()
            }
            if (mmInputStream != null) {
                mmInputStream!!.close()
            }
            if (mmSocket != null) {
                mmSocket!!.close()
            }
            changeButtonToStart()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @OnClick(R.id.talkButton)
    internal fun clickTalk() {
        val resultText = textResult!!.text.toString()

        MaterialDialog(this.requireContext()).show {
            title(text = "Select Language to Speak")
            listItemsSingleChoice(R.array.language_selection) { _, index, _ ->
                when (index) {
                    0 -> textSendListener!!.callSpeech("id", resultText, true)
                    1 -> textSendListener!!.callSpeech("kr", resultText, true)
                    else -> textSendListener!!.callSpeech("en", resultText, true)
                }
            }
        }
    }

    @OnClick(R.id.deleteResultTalkButton)
    internal fun clearText() {
        textResult!!.setText("")
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TalkFragment.
         */
        fun newInstance(): TalkFragment {
            return TalkFragment()
        }
    }

}// Required empty public constructor
