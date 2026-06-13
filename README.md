<div align="center">

# Noor — AI Quran Companion

**A voice-first Android app for blind and visually impaired Muslims. Speak naturally. Hear the Quran.**

[![Build Status](https://img.shields.io/github/actions/workflow/status/Ahmadhassan012/noor-quran/build-apk.yml?style=flat-square&label=Build)](https://github.com/Ahmadhassan012/noor-quran/actions)
![Android SDK](https://img.shields.io/badge/API-24%2B-3ddc84?style=flat-square&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7f52ff?style=flat-square&logo=kotlin)
![Compose](https://img.shields.io/badge/Compose-BOM_2024.09-4285f4?style=flat-square&logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

[Overview](#overview) • [Features](#features) • [Getting Started](#getting-started) • [Tech Stack](#tech-stack)

</div>

## Overview

Noor is a voice-first Quran companion where **the AI *is* the interface**. There are no menus, no lists, no navigation hierarchy — you speak a command in English, Urdu, or Arabic, and Noor resolves your intent and plays the authentic Arabic recitation.

Designed primarily for blind and visually impaired users who cannot rely on visual interfaces, Noor processes everything **on-device** — speech recognition, language identification, translation, and intent parsing all happen locally with zero cloud dependencies. Audio is streamed from the [cdn.islamic.network](https://cdn.islamic.network) public CDN (370+ reciters) or played from locally downloaded Surahs.

> [!NOTE]
> Noor is not a chatbot, Q&A service, or Quran learning app. It is a voice-controlled Quran player — you talk, the Quran plays.

## Features

### Voice Interaction
- **Wake word & push-to-talk** — Say "Noor" or tap the large Orb button to speak
- **Multi-language commands** — Speak in English, Urdu (اردو), or Arabic (عربي); ML Kit auto-detects and translates
- **Typed command fallback** — Text input + suggestion chips for when speech recognition is unavailable
- **Animated Noor Orb** — 5-state visual indicator (IDLE / LISTENING / PROCESSING / SPEAKING / PLAYING) with haptic feedback
- **Voice confirmation** — Noor speaks back via TTS confirming every action

### Quran Playback
- **Play any Surah** by name or number (114 Surahs with fuzzy name matching and aliases)
- **Play by Juz** (1-30) for partition-based navigation
- **Jump to a specific verse** mid-playback
- **Transport controls** — play, pause, resume, skip next/previous verse, seek ±10s
- **Auto-continue** across verses and Surahs (configurable)
- **Reciter selection** — 3 built-in (Al-Afasy, Abdul Basit, Saad Al-Ghamdi), extensible via CDN
- **Offline playback** — Download entire Surahs for offline listening
- **Background playback** — Audio continues when the screen is locked

### Data Management
- **Bookmarks** — Save and jump to verse positions, tied to Juz metadata
- **Conversation history** — Full log of user-Noor exchanges with timestamps
- **Listening history** — Track which Surahs you've played
- **Session tracking** — App usage sessions recorded in local Room database

### Accessibility
- Large touch targets throughout
- High-contrast "manuscript" light theme
- Error recovery with helpful spoken feedback
- Minimal visual dependency — all actions available via voice

### Onboarding
- 3-screen introduction (Welcome → Microphone Permission → Language Selection)
- Language selection persists to preferences
- Skippable, shown only on first launch

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Ladybug or later (for AGP 9.1.1 compatibility)
- JDK 17+
- Android SDK 36 (Android 15)
- A device or emulator running API 24+ with Google Play Services (for ML Kit)

### Run Locally

```bash
# Clone the repository
git clone https://github.com/Ahmadhassan012/noor-quran.git
cd noor-quran

# Generate debug keystore (if not present)
keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey \
  -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"

# Build and install debug APK
./gradlew installDebug
```

Or open the project in Android Studio and run on your device.

> [!TIP]
> ML Kit translation models (~20MB each for Urdu and Arabic) download automatically on first launch. A network connection is required for the initial download, after which translation works offline.

## Tech Stack

### Core

| Component | Library | Version |
|-----------|---------|---------|
| Language | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material3 | BOM 2024.09.00 |
| Navigation | Navigation Compose | 2.8.9 |
| Audio | Android MediaPlayer | SDK built-in |
| Local DB | Room | 2.7.0 |
| HTTP | OkHttp | 4.10.0 |
| Serialization | Moshi | 1.15.2 |

### AI / ML

| Component | Library | Purpose |
|-----------|---------|---------|
| STT | Android SpeechRecognizer | Speech-to-text |
| TTS | Android TextToSpeech | Voice responses |
| Language ID | ML Kit language-id 17.0.4 | Detect EN/UR/AR |
| Translation | ML Kit translate 17.0.1 | UR/AR → EN (offline) |

### Build

| Component | Version |
|-----------|---------|
| Android Gradle Plugin | 9.1.1 |
| Gradle | 9.3.1 |
| KSP | 2.3.5 |
| minSdk | 24 |
| targetSdk / compileSdk | 36 |

## Permissions

| Permission | Purpose |
|------------|---------|
| `RECORD_AUDIO` | Voice input via SpeechRecognizer |
| `INTERNET` | Stream Quran audio from CDN |
| `ACCESS_NETWORK_STATE` | Connectivity checks |

## Acknowledgments

- Quran audio provided by [cdn.islamic.network](https://cdn.islamic.network) (public CDN, 370+ reciters)
- On-device ML powered by [Google ML Kit](https://developers.google.com/ml-kit)
- Iconography from [Material Design Icons](https://fonts.google.com/icons)
