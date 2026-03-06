package com.vaishnavi.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

class BackendClient(
    val backendMode: BackendMode = BackendMode.LOCAL_ECHO,
    val backendUrl: String = "http://localhost:8080"  // OpenClaw default
) {
    
    enum class BackendMode {
        LOCAL_ECHO,              // Echo back input (offline)
        OPENCLAW_HTTP,           // OpenClaw HTTP API
        TELEGRAM_BOT,            // Telegram bot via HTTP
        OLLAMA_LOCAL             // Local Ollama (Llama 3.1:8B)
    }
    
    /**
     * Send prompt to backend and get response
     * @param prompt User's voice-recognized text
     * @return Backend's response text
     */
    suspend fun sendPrompt(prompt: String): String = withContext(Dispatchers.IO) {
        return@withContext when (backendMode) {
            BackendMode.LOCAL_ECHO -> localEcho(prompt)
            BackendMode.OPENCLAW_HTTP -> sendToOpenClaw(prompt)
            BackendMode.TELEGRAM_BOT -> sendToTelegram(prompt)
            BackendMode.OLLAMA_LOCAL -> sendToOllama(prompt)
        }
    }
    
    /**
     * Local echo (offline, no network needed)
     */
    private fun localEcho(prompt: String): String {
        return "You said: $prompt"
    }
    
    /**
     * Send to OpenClaw HTTP API
     */
    private fun sendToOpenClaw(prompt: String): String {
        return try {
            val url = URL("$backendUrl/api/message")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "Vaishnavi-Voice-App/1.0")
            
            val body = JSONObject()
            body.put("message", prompt)
            body.put("session", "vaishnavi-android")
            
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                json.optString("reply", "Sorry, I didn't understand that.")
            } else {
                "Backend error: HTTP $responseCode"
            }
        } catch (e: Exception) {
            "Connection error: ${e.message}"
        }
    }
    
    /**
     * Send to Telegram Bot API
     */
    private fun sendToTelegram(prompt: String): String {
        return try {
            // Format: https://api.telegram.org/bot{TOKEN}/sendMessage?chat_id={CHAT_ID}&text={TEXT}
            val botToken = System.getenv("TELEGRAM_BOT_TOKEN") ?: "YOUR_BOT_TOKEN"
            val chatId = System.getenv("TELEGRAM_CHAT_ID") ?: "YOUR_CHAT_ID"
            
            val message = URLEncoder.encode(prompt, "UTF-8")
            val url = URL("https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$message")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            
            if (json.optBoolean("ok")) {
                "Message sent to Telegram"
            } else {
                "Telegram error: ${json.optString("description")}"
            }
        } catch (e: Exception) {
            "Telegram error: ${e.message}"
        }
    }
    
    /**
     * Send to local Ollama (Llama 3.1:8B)
     */
    private fun sendToOllama(prompt: String): String {
        return try {
            val url = URL("http://localhost:11434/api/generate")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            
            val body = JSONObject()
            body.put("model", "llama2")
            body.put("prompt", prompt)
            body.put("stream", false)
            
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray())
            }
            
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            json.optString("response", "Ollama error: no response")
        } catch (e: Exception) {
            "Ollama error: ${e.message} (Is Ollama running on localhost:11434?)"
        }
    }
    
    /**
     * Check if the remote backend is reachable by calling its /health endpoint.
     * Returns true if the server responds with HTTP 200, false otherwise.
     * Only meaningful for OPENCLAW_HTTP mode.
     */
    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        if (backendMode != BackendMode.OPENCLAW_HTTP) return@withContext false
        return@withContext try {
            val url = URL("$backendUrl/health")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: IOException) {
            false
        }
    }
}
