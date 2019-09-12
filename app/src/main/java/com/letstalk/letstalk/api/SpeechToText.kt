package com.letstalk.letstalk.api

import com.letstalk.letstalk.model.SpeechToTextResponse

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SpeechToText {
    @Multipart
    @POST("upload_file")
    fun speechToText(@Part audio: MultipartBody.Part): Call<SpeechToTextResponse>
}
