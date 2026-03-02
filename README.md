# Vaishnavi (Voice-Only Android Assistant)

A minimal, beautiful **voice-only** Android app that listens, thinks, and speaks back — no chat thread. Designed for live, interruptible conversations.

## Features
- Single-screen UI with clear states: **Listening / Thinking / Speaking**
- Large mic button to toggle listening on/off
- Android **SpeechRecognizer** (STT)
- Android **TextToSpeech** (TTS), prefers **English (India)** voice when available
- Interruptible: if you start speaking while TTS is speaking, it stops and listens
- Stub backend (echo) so the app runs offline; Telegram/OpenClaw hookup can be added later

## Requirements
- Android Studio (Giraffe+ recommended)
- Android SDK 34
- Min SDK 26

## Permissions
- `RECORD_AUDIO`
- `INTERNET`

## Run
1. Open this project in Android Studio.
2. Let Gradle sync.
3. Run on a device (mic permission will be requested on first use).

## Next Steps
- Replace `BackendClient` with Telegram/OpenClaw integration.
- Add richer visuals or animations for state transitions.

---
Built for Harsh with ❤️ by Vaishnavi.
