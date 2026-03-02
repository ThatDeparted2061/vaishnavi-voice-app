package com.vaishnavi.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class BackendClient {
    suspend fun sendPrompt(prompt: String): String = withContext(Dispatchers.IO) {
        // Stub: replace with Telegram/OpenClaw backend later
        delay(400)
        "I heard: $prompt"
    }
}
