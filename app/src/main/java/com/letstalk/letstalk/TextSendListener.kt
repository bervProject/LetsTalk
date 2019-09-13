package com.letstalk.letstalk

/**
 * TextSendListener
 * Interface to call speech from fragment into activity
 */
interface TextSendListener {
    fun callSpeech(text: String)

    fun callSpeech(text: String, flushMode: Boolean)

    fun callSpeech(language: String, text: String, flushMode: Boolean)
}
