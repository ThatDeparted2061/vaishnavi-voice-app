package com.vaishnavi.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SpeechRecognizerManager.Listener {

    private lateinit var stateText: TextView
    private lateinit var micButton: ImageButton
    private lateinit var connectSwitch: SwitchCompat

    private lateinit var speech: SpeechRecognizerManager
    private lateinit var tts: TtsManager

    // Default to LOCAL_ECHO so no remote server connection is made on startup.
    // Switch ON connects to the remote OpenClaw backend on port 9000.
    @Volatile private var backend = BackendClient(BackendClient.BackendMode.LOCAL_ECHO)

    private val remoteBackendUrl = "http://10.30.12.249:9000"

    private var currentState: VoiceState = VoiceState.IDLE

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stateText = findViewById(R.id.stateText)
        micButton = findViewById(R.id.micButton)
        connectSwitch = findViewById(R.id.connectSwitch)

        speech = SpeechRecognizerManager(this)
        speech.setListener(this)

        tts = TtsManager(this)
        tts.onStart = { setState(VoiceState.SPEAKING) }
        tts.onDone = { if (currentState == VoiceState.SPEAKING) setState(VoiceState.IDLE) }

        micButton.setOnClickListener {
            if (currentState == VoiceState.LISTENING) stopListening() else checkPermissionAndStart()
        }

        // Wire the switch: OFF = LOCAL_ECHO (no server needed),
        // ON = OPENCLAW_HTTP with a one-time health check.
        connectSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                backend = BackendClient(BackendClient.BackendMode.OPENCLAW_HTTP, remoteBackendUrl)
                stateText.text = "Checking server…"
                lifecycleScope.launch {
                    val healthy = backend.checkHealth()
                    stateText.text = if (healthy) "Server connected" else "Server unreachable"
                }
            } else {
                backend = BackendClient(BackendClient.BackendMode.LOCAL_ECHO)
                stateText.text = "Tap to speak"
            }
        }

        setState(VoiceState.IDLE)
    }

    private fun checkPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ->
                startListening()
            else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startListening() {
        tts.stop()
        setState(VoiceState.LISTENING)
        speech.startListening()
    }

    private fun stopListening() {
        speech.stopListening()
        setState(VoiceState.IDLE)
    }

    private fun setState(state: VoiceState) {
        currentState = state
        val label = when (state) {
            VoiceState.IDLE -> "Tap to speak"
            VoiceState.LISTENING -> "Listening…"
            VoiceState.THINKING -> "Thinking…"
            VoiceState.SPEAKING -> "Speaking…"
        }
        stateText.text = label
        micButton.isSelected = state == VoiceState.LISTENING
    }

    override fun onSpeechStart() {
        if (currentState == VoiceState.SPEAKING) {
            tts.stop()
            setState(VoiceState.LISTENING)
        }
    }

    override fun onSpeechEnd() {
        // wait for results
    }

    override fun onSpeechResult(text: String) {
        setState(VoiceState.THINKING)
        lifecycleScope.launch {
            val reply = backend.sendPrompt(text)
            tts.speak(reply)
        }
    }

    override fun onError() {
        setState(VoiceState.IDLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        speech.destroy()
        tts.shutdown()
    }
}
