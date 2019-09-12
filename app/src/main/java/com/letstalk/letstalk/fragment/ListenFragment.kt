package com.letstalk.letstalk.fragment

import android.Manifest
import android.app.AlertDialog
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.letstalk.letstalk.R
import com.letstalk.letstalk.TextSendListener
import com.letstalk.letstalk.api.SpeechToText
import com.letstalk.letstalk.model.SpeechToTextResponse
import com.readystatesoftware.chuck.ChuckInterceptor

import java.io.File
import java.io.IOException
import java.util.Locale
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import dmax.dialog.SpotsDialog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * A simple [Fragment] subclass.
 * Use the [ListenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListenFragment : Fragment(), EasyPermissions.PermissionCallbacks {


    private val RC_AUDIO_RECORD = 10
    private var mRecorder: MediaRecorder? = null
    private var newFilePath: String? = null
    private var textSendListener: TextSendListener? = null
    private var serviceSpeechToText: SpeechToText? = null
    private var loading: AlertDialog? = null
    private var resultTextFromSpeech: Call<SpeechToTextResponse>? = null

    private val URL_SERVER = "http://35.240.163.60:5000/"

    @BindView(R.id.micButton)
    @JvmField
    var micButton: ImageButton? = null
    @BindView(R.id.stopButton)
    @JvmField
    var stopButton: ImageButton? = null
    @BindView(R.id.resultListenBox)
    @JvmField
    var resultBox: EditText? = null
    @BindView(R.id.talkResultButton)
    @JvmField
    var speakButton: ImageButton? = null
    private val callbackResponse = object : Callback<SpeechToTextResponse> {
        override fun onResponse(call: Call<SpeechToTextResponse>, response: Response<SpeechToTextResponse>) {
            if (response.isSuccessful) {
                val serverResponse = response.body()
                if (serverResponse != null) {
                    val status = serverResponse.status
                    val result = serverResponse.result
                    if (status != null) {
                        if (status.equals("success", ignoreCase = true)) {
                            if (result != null) {
                                resultBox!!.setText(result)
                            } else {
                                resultBox!!.setText("")
                                Toast.makeText(activity, "No Result from Server", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(activity, "Failed to Send Data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "Different Format Receive from Server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Different Format Receive from Server", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show()
            }
            loading!!.dismiss()
        }

        override fun onFailure(call: Call<SpeechToTextResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
            loading!!.dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_listen, container, false)
        ButterKnife.bind(this, view)
        resultBox!!.keyListener = null
        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @OnClick(R.id.micButton)
    internal fun micClick() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this.context!!, *perms)) {
            recordAudio()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.audio_record),
                    RC_AUDIO_RECORD, *perms)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is TextSendListener) {
            textSendListener = activity as TextSendListener?
        }
    }

    @OnClick(R.id.talkResultButton)
    internal fun speak() {
        if (textSendListener != null) {
            val resultText = resultBox!!.text.toString()

            MaterialDialog(this.context!!).show {
                title(text = "Select Language to Speak")
                listItemsSingleChoice(R.array.language_selection) {
                    _, index, _ ->
                    when (index) {
                        0 -> textSendListener!!.callSpeech("id", resultText, true)
                        1 -> textSendListener!!.callSpeech("kr", resultText, true)
                        else -> textSendListener!!.callSpeech("en", resultText, true)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        showMic()
        prepareRetrofit()
        prepareDialog()
    }

    private fun prepareDialog() {
        loading = SpotsDialog.Builder().setContext(this.requireContext()).build()
    }

    private fun showMic() {
        micButton!!.visibility = View.VISIBLE
        stopButton!!.visibility = View.GONE
    }

    private fun showStop() {
        stopButton!!.visibility = View.VISIBLE
        micButton!!.visibility = View.GONE
    }

    @OnClick(R.id.stopButton)
    internal fun stopRecord() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
        showMic()
        sendingFile()
    }

    private fun sendingFile() {
        if (newFilePath != null) {
            if (serviceSpeechToText != null) {
                loading!!.show()
                val file = File(newFilePath!!)
                val requestBody = RequestBody.create(MediaType.parse("audio/*"), file)
                val body = MultipartBody.Part.createFormData("audio",
                        file.name,
                        requestBody)
                resultTextFromSpeech = serviceSpeechToText!!.speechToText(body)
                resultTextFromSpeech!!.enqueue(callbackResponse)
            } else {
                Toast.makeText(activity, "The devices not ready to send files to server.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "Please record your voice first please", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RC_AUDIO_RECORD) {
            recordAudio()
        }
    }

    private fun recordAudio() {
        val cacheDir = this.context!!.cacheDir
        val newPath = File(cacheDir, "letstalk")
        var success = true
        if (!newPath.exists()) {
            success = newPath.mkdir()
        }
        if (success) {
            val nameFile = String.format(Locale.ENGLISH, "new-record-%d.aac", System.currentTimeMillis())
            val newAudio = File(newPath, nameFile)
            newFilePath = newAudio.absolutePath
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            mRecorder!!.setOutputFile(newFilePath)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                mRecorder!!.prepare()
                mRecorder!!.start()
                showStop()
            } catch (e: IOException) {
                val message = "Prepare failed :" + e.message
                Log.e("Recorder_Lets_Talk", message)
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(activity, "Can't Create Directory", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prepareRetrofit() {
        val client = OkHttpClient.Builder()
                .addInterceptor(ChuckInterceptor(this.context!!.applicationContext))
                .build()
        val retrofit = Retrofit.Builder()
                .baseUrl(URL_SERVER)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        serviceSpeechToText = retrofit.create(SpeechToText::class.java)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ListenFragment.
         */
        fun newInstance(): ListenFragment {
            return ListenFragment()
        }
    }

}// Required empty public constructor
