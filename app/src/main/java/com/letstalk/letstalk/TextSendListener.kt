package com.letstalk.letstalk

interface TextSendListener {
    fun callSpeech(text: String)

    fun callSpeech(text: String, flushMode: Boolean)

    fun callSpeech(language: String, text: String, flushMode: Boolean)
}
