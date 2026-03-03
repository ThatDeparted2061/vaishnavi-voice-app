# 🗣️ Vaishnavi Voice App — Backend Integration Guide

## Overview

Vaishnavi is a **voice-only Android assistant** that supports 4 backend modes:

1. **Local Echo** (Offline) — Echo back user input
2. **OpenClaw HTTP** — Connect to OpenClaw gateway via HTTP
3. **Telegram Bot** — Send messages to Telegram
4. **Ollama Local** — Use local Llama 3.1:8B model

---

## Setup Instructions

### 0. Prerequisites

```bash
# Install Android SDK (if not already done)
# User needs to run this manually due to TTY limitations
sudo apt-get install android-sdk

# Verify gradle wrapper
cd vaishnavi-voice-app
chmod +x gradlew
```

### 1. Configure Backend Mode

Edit `app/src/main/java/com/vaishnavi/voice/MainActivity.kt`:

```kotlin
// Change this line:
private val backend = BackendClient()

// To one of:
private val backend = BackendClient(BackendClient.BackendMode.LOCAL_ECHO)
private val backend = BackendClient(BackendClient.BackendMode.OPENCLAW_HTTP)
private val backend = BackendClient(BackendClient.BackendMode.TELEGRAM_BOT)
private val backend = BackendClient(BackendClient.BackendMode.OLLAMA_LOCAL)
```

---

## Backend Mode Details

### Mode 1: LOCAL_ECHO (Default — Offline)

**No setup needed!** Echoes back user input.

```
User: "Hello"
App: "You said: Hello"
```

✅ Works offline  
❌ No actual AI

---

### Mode 2: OPENCLAW_HTTP

**Requires:** OpenClaw gateway running + HTTP API accessible

#### Setup:

1. **Verify OpenClaw is running:**
   ```bash
   openclaw status
   ```

2. **Get gateway URL** (usually `http://localhost:8080`)

3. **Edit MainActivity.kt:**
   ```kotlin
   private val backend = BackendClient(
       BackendClient.BackendMode.OPENCLAW_HTTP,
       "http://localhost:8080"  // Change if needed
   )
   ```

4. **Ensure network access:**
   - Android app must reach your OpenClaw machine
   - If on different machine: use WiFi IP (e.g., `http://192.168.1.100:8080`)

#### Expected Flow:
```
User: "What is the weather?"
↓
App sends HTTP POST to OpenClaw
↓
OpenClaw processes via Telegram session
↓
Response comes back: "Current weather is..."
```

---

### Mode 3: TELEGRAM_BOT

**Requires:** Telegram bot token + chat ID

#### Setup:

1. **Get Telegram Bot Token:**
   - Talk to @BotFather on Telegram
   - Create new bot: `/newbot`
   - Copy the token (e.g., `123456789:ABCdef...`)

2. **Get Your Chat ID:**
   - Message the bot
   - Go to: `https://api.telegram.org/bot{TOKEN}/getUpdates`
   - Find your message, extract `chat_id`

3. **Set environment variables:**
   ```bash
   export TELEGRAM_BOT_TOKEN="your_token_here"
   export TELEGRAM_CHAT_ID="your_chat_id_here"
   ```

4. **Edit MainActivity.kt:**
   ```kotlin
   private val backend = BackendClient(BackendClient.BackendMode.TELEGRAM_BOT)
   ```

#### Expected Flow:
```
User: "Hello"
↓
App sends to Telegram Bot API
↓
Message arrives in your Telegram chat
↓
Response: "Message sent to Telegram"
```

---

### Mode 4: OLLAMA_LOCAL

**Requires:** Ollama running + Llama 2 model downloaded

#### Setup:

1. **Install Ollama:**
   ```bash
   curl https://ollama.ai/install.sh | sh
   ollama serve  # Start in background
   ```

2. **Download Llama 2:**
   ```bash
   ollama pull llama2
   ```

3. **Edit MainActivity.kt:**
   ```kotlin
   private val backend = BackendClient(BackendClient.BackendMode.OLLAMA_LOCAL)
   ```

4. **Verify Ollama is accessible:**
   ```bash
   curl http://localhost:11434/api/tags
   ```

#### Expected Flow:
```
User: "What is 2+2?"
↓
App sends to Ollama (localhost:11434)
↓
Llama 2 processes locally
↓
Response: "2+2 equals 4"
```

---

## Building APK

### Prerequisites:
```bash
# Install Android SDK (one-time)
sudo apt-get install -y android-sdk
export ANDROID_SDK_ROOT=/usr/lib/android-sdk

# Or download manually:
# https://developer.android.com/studio
```

### Build Steps:

```bash
cd vaishnavi-voice-app

# Make gradle executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK (if keys configured)
./gradlew assembleRelease

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Install on Device:

```bash
# Via ADB (Android Debug Bridge)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or manually:
# - Download APK from app/build/outputs/apk/debug/
# - Transfer to Android device
# - Open & install
```

---

## Permissions Required

Android permissions automatically requested:

- `RECORD_AUDIO` — Microphone access
- `INTERNET` — Network access (for OpenClaw/Telegram/Ollama)

App requests permissions at runtime when user taps mic button.

---

## Troubleshooting

### "Connection error: Connection refused"
- Backend service not running
- Wrong IP/port
- Firewall blocking

**Fix:**
```bash
# Test connectivity
curl http://localhost:8080/api/status
ping 192.168.1.100  # Test IP
```

### "Ollama error: Is Ollama running?"
- Ollama not started
- Not on port 11434

**Fix:**
```bash
ollama serve
# Should say "Listening on 127.0.0.1:11434"
```

### "Telegram error: HTTP 401"
- Bot token invalid
- Token expired

**Fix:** Re-create bot with @BotFather

### App crashes when building
- Android SDK not installed
- Gradle cache corrupt

**Fix:**
```bash
./gradlew clean
./gradlew assembleDebug
```

---

## Development Notes

### Adding Custom Backend

Edit `BackendClient.kt`:

```kotlin
// 1. Add new mode
enum class BackendMode {
    // ...
    MY_CUSTOM_BACKEND
}

// 2. Add handler
suspend fun sendPrompt(prompt: String): String = withContext(Dispatchers.IO) {
    return@withContext when (backendMode) {
        // ...
        BackendMode.MY_CUSTOM_BACKEND -> myCustomBackend(prompt)
    }
}

// 3. Implement
private fun myCustomBackend(prompt: String): String {
    // Your logic here
}
```

### Real-time Updates

To support streaming responses (for Ollama/OpenClaw):

1. Change return type: `suspend fun sendPrompt(prompt: String): Flow<String>`
2. Emit chunks as they arrive
3. Update UI in MainActivity to handle streaming

---

## Example: Full Workflow (OpenClaw Mode)

### 1. User speaks: "What's the weather?"

### 2. STT converts to text: "What's the weather?"

### 3. App sends to OpenClaw:
```
POST http://localhost:8080/api/message
{
  "message": "What's the weather?",
  "session": "vaishnavi-android"
}
```

### 4. OpenClaw processes:
- Routes to Telegram session
- Executes with Claude/Haiku
- Uses web_search tool
- Gets weather data

### 5. Response comes back:
```json
{
  "reply": "Current weather in Hyderabad is 28°C, partly cloudy"
}
```

### 6. TTS speaks the response:
🔊 "Current weather in Hyderabad is 28°C, partly cloudy"

---

## Files Reference

| File | Purpose |
|---|---|
| `MainActivity.kt` | UI + state machine |
| `BackendClient.kt` | Backend communication (4 modes) |
| `SpeechRecognizerManager.kt` | STT (voice → text) |
| `TtsManager.kt` | TTS (text → speech, en-IN) |
| `VoiceState.kt` | State enum (IDLE/LISTENING/THINKING/SPEAKING) |
| `activity_main.xml` | UI layout |

---

## Next Steps

1. ✅ Code is complete
2. ⏳ Install Android SDK (manual step)
3. ⏳ Choose backend mode
4. ⏳ Build APK: `./gradlew assembleDebug`
5. ⏳ Install on device via ADB
6. ⏳ Test with chosen backend

---

**Questions?** Check `README.md` or edit `BackendClient.kt` for your setup! 🚀
