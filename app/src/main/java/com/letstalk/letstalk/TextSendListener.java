package com.letstalk.letstalk;

public interface TextSendListener {
    void callSpeech(String text);
    void callSpeech(String text, boolean flushMode);
}
