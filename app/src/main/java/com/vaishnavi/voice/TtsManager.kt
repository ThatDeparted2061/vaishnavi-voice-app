package com.vaishnavi.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    var onStart: (() -> Unit)? = null
    var onDone: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                val locale = Locale("en", "IN")
                tts?.language = locale
                tts?.setSpeechRate(1.0f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { onStart?.invoke() }
                    override fun onDone(utteranceId: String?) { onDone?.invoke() }
                    override fun onError(utteranceId: String?) { onDone?.invoke() }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vaishnavi-utterance")
    }

    fun stop() { tts?.stop() }

    fun shutdown() { tts?.shutdown() }
}
