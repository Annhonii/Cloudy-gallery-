<div align="center">

# ◆ Cloudy Gallery

**A Nothing OS-inspired gallery with Telegram cloud sync and end-to-end encryption**

[![Build Debug APK](https://github.com/Annhonii/Cloudy-gallery-/actions/workflows/build.yml/badge.svg)](https://github.com/Annhonii/Cloudy-gallery-/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Annhonii/Cloudy-gallery-?style=flat&logo=github)](https://github.com/Annhonii/Cloudy-gallery-/releases/latest)
[![License](https://img.shields.io/badge/license-Apache%202.0-black.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-26%2B-black.svg)](https://developer.android.com/studio/releases/platforms)

</div>

---

## ◆ Overview

**Cloudy Gallery** is a modern Android gallery application built with Jetpack Compose and Material 3. Inspired by the minimalist monochrome aesthetic of Nothing OS, it combines beautiful UI with powerful privacy features.

Your photos are **yours**. Even in the cloud.

---

## ◆ Features

| Feature | Description |
|---|---|
| **Nothing OS Aesthetic** | Monochrome design, dot-matrix typography, sharp geometric shapes |
| **Material 3** | Full Material You with dynamic animations and transitions |
| **Local Gallery** | Browse all photos and videos from device storage |
| **Telegram Cloud Sync** | Use your own Telegram bot to sync photos to a private group |
| **AES-256-GCM Encryption** | Military-grade encryption — photos stored as opaque `.bin` files |
| **Per-Photo Unique Keys** | Each photo has its own random salt and IV — no two files are alike |
| **On-Demand Buffering** | Cloud photos streamed and cached locally only when viewed |
| **Privacy First** | Master password never leaves the device; bot token stored in EncryptedSharedPreferences |

---

## ◆ How Encryption Works

Every photo is encrypted before leaving your device:

```
Photo bytes
    │
    ▼
Random 16-byte Salt  ──►  PBKDF2WithHmacSHA256(masterPassword, salt, 310,000 iterations)  ──►  256-bit Key
Random 12-byte IV    ──►  AES-256-GCM encrypt(photo, key, iv)  ──►  Ciphertext
    │
    ▼
.bin file = [CGAL magic (4B)] [salt (16B)] [IV (12B)] [original size (4B)] [ciphertext]
```

- **310,000 PBKDF2 iterations** make brute-force attacks infeasible
- **Unique salt per photo** means identical photos produce different `.bin` files
- **GCM authentication tag** detects tampering
- Photos in Telegram look like random binary blobs — unreadable without your master password

---

## ◆ Setup

### Prerequisites
- Android 8.0+ (API 26)
- A Telegram account
- [BotFather](https://t.me/BotFather) to create a bot

### Telegram Bot Setup
1. Open Telegram → search `@BotFather` → `/newbot`
2. Note your **Bot Token** (e.g. `1234567890:ABCdef...`)
3. Create a **private Telegram group** and add your bot as an admin
4. Get the **Chat ID** (use `@userinfobot` or the bot API)
5. Open Cloudy Gallery → Settings → Enter Bot Token + Chat ID

### First Launch
1. Install the APK from [Releases](https://github.com/Annhonii/Cloudy-gallery-/releases/latest)
2. Grant storage permissions
3. Set a **Master Password** (this encrypts/decrypts all cloud photos)
4. Connect your Telegram bot
5. Start syncing!

---

## ◆ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture + Hilt DI
- **Database**: Room
- **Networking**: Ktor (Telegram Bot API)
- **Image Loading**: Coil 3
- **Encryption**: javax.crypto (AES-256-GCM + PBKDF2)
- **Background Sync**: WorkManager
- **Security**: EncryptedSharedPreferences

---

## ◆ Screenshots

> *Coming soon*

---

## ◆ Building from Source

```bash
git clone https://github.com/Annhonii/Cloudy-gallery-.git
cd Cloudy-gallery-
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/
```

### Release Build & Signing
See [`.github/workflows/release.yml`](.github/workflows/release.yml) for CI/CD setup.
Required secrets: `KEYSTORE_BASE64`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_PASSWORD`

---

## ◆ License

```
Copyright 2024 Annhonii

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<div align="center">
Made with ◆ by Annhonii
</div>
