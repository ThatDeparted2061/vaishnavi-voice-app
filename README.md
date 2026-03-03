# 🗣️ Vaishnavi Voice App

**Voice-only Android assistant** — STT + AI response + TTS, no chat history.

Named after **Vaishnavi**, a dear friend who passed away. Her memory lives on in this app. 👻

---

## Features

✅ **Voice Input** — Speech-to-Text (en-IN locale)  
✅ **AI Processing** — 4 backend modes supported  
✅ **Voice Output** — Text-to-Speech (en-IN voice)  
✅ **Interruptible** — Tap to stop TTS and speak again  
✅ **No History** — Each conversation is ephemeral  
✅ **Minimal UI** — Single button + status text  

---

## Supported Backends

| Mode | Features | Setup |
|---|---|---|
| **Local Echo** | Offline echo (no AI) | None |
| **OpenClaw HTTP** | Full AI via OpenClaw gateway | Runs locally |
| **Telegram Bot** | Send to Telegram | Bot token required |
| **Ollama Local** | Llama 3.1:8B locally | Download model |

---

## Quick Start

### 1. Prerequisites

```bash
# Android SDK (one-time)
sudo apt-get install -y android-sdk
export ANDROID_SDK_ROOT=/usr/lib/android-sdk
```

### 2. Build APK

```bash
cd vaishnavi-voice-app
chmod +x gradlew
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install on Device

```bash
# Via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or transfer APK manually to device
```

### 4. Choose Backend

Edit `MainActivity.kt` line ~25:

```kotlin
private val backend = BackendClient(BackendClient.BackendMode.LOCAL_ECHO)
// Options: LOCAL_ECHO, OPENCLAW_HTTP, TELEGRAM_BOT, OLLAMA_LOCAL
```

Rebuild APK after changing backend.

### 5. Run

- Open app on Android device
- Tap mic button
- Speak your question
- App listens, processes, responds with voice

---

## Backend Setup

Full backend configuration guide: **[BACKEND_SETUP.md](./BACKEND_SETUP.md)**

### Quick Examples:

**OpenClaw (Full AI):**
```kotlin
private val backend = BackendClient(
    BackendClient.BackendMode.OPENCLAW_HTTP,
    "http://localhost:8080"
)
```

**Local Ollama (Private LLM):**
```kotlin
private val backend = BackendClient(BackendClient.BackendMode.OLLAMA_LOCAL)
// Requires: ollama serve + ollama pull llama2
```

**Telegram (Simple Bot):**
```kotlin
private val backend = BackendClient(BackendClient.BackendMode.TELEGRAM_BOT)
// Requires: TELEGRAM_BOT_TOKEN + TELEGRAM_CHAT_ID env vars
```

---

## Architecture

```
┌─────────────────────────────────────┐
│  MainActivity                       │
│  - State Machine (IDLE/LISTENING...) │
│  - Button handling                  │
└──────────────┬──────────────────────┘
               │
     ┌─────────┴──────────┐
     │                    │
  STT                    TTS
  Speech         Text-to-Speech
  Recognizer      (en-IN voice)
  (en-IN)              │
     │                 │
     └────────┬────────┘
              │
         ┌────▼─────┐
         │ BackendClient
         ├──────────────┐
         │ 4 modes:    │
         │ - Echo      │
         │ - OpenClaw  │
         │ - Telegram  │
         │ - Ollama    │
         └────────────┘
```

---

## Project Structure

```
vaishnavi-voice-app/
├── app/src/main/java/com/vaishnavi/voice/
│   ├── MainActivity.kt              # UI + state machine
│   ├── BackendClient.kt             # 4 backend modes
│   ├── SpeechRecognizerManager.kt    # STT (voice → text)
│   ├── TtsManager.kt                # TTS (text → speech)
│   ├── VoiceState.kt                # State enum
│   └── ...
├── app/src/main/res/
│   ├── layout/activity_main.xml     # UI layout
│   └── ...
├── build.gradle.kts                 # Dependencies
├── README.md                         # This file
├── BACKEND_SETUP.md                 # Backend configuration
└── .git/                             # GitHub repo
```

---

## Permissions

App requests at runtime:

- `RECORD_AUDIO` — Microphone access
- `INTERNET` — Network (for OpenClaw/Telegram/Ollama)

---

## Troubleshooting

### Build fails: "SDK not found"
```bash
# Install Android SDK
sudo apt-get install android-sdk
export ANDROID_SDK_ROOT=/usr/lib/android-sdk
./gradlew assembleDebug
```

### App crashes on startup
- Check permissions are granted
- Verify backend is reachable (if not Local Echo)
- Check Logcat: `adb logcat | grep Vaishnavi`

### No audio output
- Check device volume isn't muted
- Verify TTS initialization in `TtsManager.kt`
- Test system TTS: Settings → Accessibility → Text-to-Speech

### Backend not responding
- See **[BACKEND_SETUP.md](./BACKEND_SETUP.md)** for each mode

---

## Development

### Adding Custom Backend

1. Add mode to `BackendClient.BackendMode` enum
2. Implement handler function
3. Add case to `sendPrompt()` when statement
4. Rebuild APK

### Streaming Responses

For real-time response streaming:
1. Change `sendPrompt()` to return `Flow<String>`
2. Emit chunks as they arrive
3. Update UI to handle streaming in `MainActivity`

### Local Testing

```bash
# Test with local echo (no backend needed)
# See BACKEND_SETUP.md for other modes
```

---

## GitHub

📦 **Repository:** https://github.com/ThatDeparted2061/vaishnavi-voice-app  
🔐 **Visibility:** Private

---

## Next Steps

1. ✅ Code complete
2. ⏳ Install Android SDK
3. ⏳ Choose backend mode
4. ⏳ Build APK: `./gradlew assembleDebug`
5. ⏳ Install on device
6. ⏳ Test!

---

**For detailed backend setup:** See [BACKEND_SETUP.md](./BACKEND_SETUP.md)

**Questions?** Check the troubleshooting section or review the backend guide.

---

_Named after Vaishnavi, forever in our hearts. 👻_
