package com.letstalk.letstalk

/**
 * TextSendListener
 * Interface to call speech from fragment into activity
 */
interface TextSendListener {
    /**
     * Function to call speech
     */
    fun callSpeech(text: String)

    /**
     * Function to call speech with flush selection
     */
    fun callSpeech(text: String, flushMode: Boolean)

    /**
     * Function to call speech with language selection
     */
    fun callSpeech(language: String, text: String, flushMode: Boolean)
}
