<p align="center"><pre>
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
/\      .S_sSSs      sSSs_sSSs    sdSS_SSSSSSbs    sSSs    sSSs /\
\/     .SS~YS%%b    d%%SP~YS%%b   YSSS~S%SSSSSP   d%%SP   d%%SP \/
/\     S%S   `S%b  d%S'     `S%b       S%S       d%S'    d%S'   /\
\/     S%S    S%S  S%S       S%S       S%S       S%S     S%|    \/
/\     S%S    S&S  S&S       S&S       S&S       S&S     S&S    /\
\/     S&S    S&S  S&S       S&S       S&S       S&S_Ss  Y&Ss   \/
/\     S&S    S&S  S&S       S&S       S&S       S&S~SP  `S&&S  /\
\/     S&S    S&S  S&S       S&S       S&S       S&S       `S*S \/
/\     S*S    S*S  S*b       d*S       S*S       S*b        l*S /\
\/     S*S    S*S  S*S.     .S*S       S*S       S*S.      .S*P \/
/\     S*S    S*S   SSSbs_sdSSS        S*S        SSSbs  sSS*S  /\
\/     S*S    SSS    YSSP~YSSY         S*S         YSSP  YSS'   \/
/\     SP                              SP                       /\
\/     Y                              Y                        \/
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/                  
                                              
</pre></p>

```
┌─────────────────────────────────────────────────────────────────────────┐
│  A private, security-hardened Android notes app.                        │
│  AES-256-GCM encryption for every note. Always on. No opt-in.          │
│                                                                         │
│  Fork of EasyNotes by Kin69 — cloud sync and donation systems          │
│  stripped out. Encryption and authentication rebuilt from scratch.      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## WHAT'S DIFFERENT FROM EASYNOTES

```
┌─────────────────────────────────────────────────────────────────────────┐
│  REMOVED                                                                │
├─────────────────────────────────────────────────────────────────────────┤
│  Cloud / NextCloud sync                                                 │
│  Fingerprint and pattern lock (cannot derive AES key from biometrics)  │
│  Separate vault mode / vault toggle / vault lock icon                  │
│  Support links, donation prompts, Discord, Instagram, feature requests │
│  Gallery sync setting                                                   │
│  Tools section (note count display)                                     │
│  Unencrypted backup option                                              │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  CHANGED                                                                │
├─────────────────────────────────────────────────────────────────────────┤
│  App name and package ID (com.notes.app)                                │
│  All branding references to EasyNotes removed                           │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  ADDED / HARDENED                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│  Single unified password — one password locks app + encrypts all notes  │
│  AES-256-GCM on all notes at all times — no opt-in required            │
│  PBKDF2-HMAC-SHA256 at 120,000 iterations (was single-pass SHA-256)    │
│  Master key derived once at login, cached in memory — no per-note KDF  │
│  Passcode stored as salted PBKDF2 hash (was plaintext in DataStore)    │
│  Backup always encrypted — no separate prompt, no plaintext option     │
│  Restore prompts backup password separately — cross-device restore     │
│    supported, re-encrypts all notes to current password after restore  │
│  Widget auth bypass fixed (confirmed Android 16 / Pixel Fold)          │
│  getDefaultRoute() fixed to check all auth methods, not just passcode  │
│  loadDefaultRoute() typo fixed (== -> =) that prevented route being set│
│  data_extraction_rules.xml — cloud backup + device transfer blocked    │
│  FLAG_SECURE before first frame (splash was previously unprotected)    │
│  First-launch setup screen — app unusable without setting a password   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## SECURITY MODEL

### Version 1.7.1 (Legacy - Deprecated)

```
┌─────────────────────────────────────────────────────────────────────────┐
│  PASSWORD VERIFICATION                                                  │
│    └─> PBKDF2-HMAC-SHA256 / 120,000 iterations                         │
│         └─> CPU-only — highly parallelizable on GPUs/ASICs             │
│                                                                         │
│  KEY MANAGEMENT                                                         │
│    └─> AES-256-GCM master key derived from password via PBKDF2         │
│         └─> Key material exists in JVM heap as SecretKey object        │
│              └─> Accessible to memory dump on rooted device            │
│                                                                         │
│  SESSION LIFECYCLE                                                      │
│    └─> Key cached in memory for session duration                       │
│         └─> Auto-lock only on process kill — no timeout                │
│              └─> Notes list stays in RAM when app backgrounds          │
│                                                                         │
│  BACKUP                                                                 │
│    └─> Zip encrypted with session password (AES-256-CBC, PBKDF2)       │
│         └─> Notes inside already AES-256-GCM encrypted                 │
│              └─> False confidence — data still extractable with key    │
│                                                                         │
│  DEVICE SECURITY                                                        │
│    └─> No screen lock enforcement                                      │
│         └─> App runs on devices with no PIN/password                   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Version 2.0.0 (Current - Recommended)

```
┌─────────────────────────────────────────────────────────────────────────┐
│  PASSWORD VERIFICATION                                                  │
│    └─> Argon2id — 64MB RAM / 3 iterations                              │
│         └─> Memory-hard — GPU/ASIC resistant                           │
│              └─> Winner of Password Hashing Competition                │
│                                                                         │
│  KEY MANAGEMENT (Hardware-Backed)                                       │
│    └─> AES-256-GCM keys generated inside TEE/StrongBox                 │
│         └─> Non-extractable — keys never leave secure hardware         │
│              └─> Raw key bytes never enter JVM heap                    │
│                   └─> Extraction impossible even with root access      │
│                                                                         │
│  BIOMETRICS                                                             │
│    └─> Full androidx.biometric framework support                       │
│         └─> Fingerprint / Face / System PIN/Pattern                    │
│              └─> Authorizes sessions without exposing master passcode  │
│                                                                         │
│  SESSION LIFECYCLE                                                      │
│    └─> Auto-lock engine — configurable timer (1, 5, 15, 30 min)        │
│         └─> Memory zeroization on lock                                 │
│              └─> Decrypted notes wiped from RAM explicitly             │
│                   └─> Session tokens cleared                           │
│                                                                         │
│  BACKUP STRATEGY                                                        │
│    └─> Device-bound — keys tied to hardware                            │
│         └─> Traditional cloud/ADB backups disabled                     │
│              └─> Honest security — data stays on device                │
│                   └─> Plaintext export available (with security warning)│
│                                                                         │
│  DEVICE SECURITY                                                        │
│    └─> Screen lock enforced at OS level                                │
│         └─> App refuses to run if device lacks PIN/pattern/password    │
│              └─> Ensures StrongBox/TEE has secure foundation           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## SECURITY EVOLUTION: V1 → V2

| Feature | Old Model (V1.7.1) | New Model (V2.0.0) |
| :--- | :--- | :--- |
| **Password Hashing** | PBKDF2-HMAC-SHA256 (120k iterations) | Argon2id (64MB RAM / 3 iterations) |
| **GPU/ASIC Resistance** | Weak — CPU-only, highly parallelizable | Strong — Memory-hard, prevents mass cracking |
| **AES Key Generation** | Derived from password in software | Hardware-generated (TEE/StrongBox) |
| **Key Extraction** | Possible via Root + Memory Dump | Physically impossible — key never leaves hardware |
| **JVM Heap Safety** | Key material exists in JVM memory | Zero-leakage — raw key bytes never enter heap |
| **Biometrics** | Not supported | Full support (Fingerprint, PIN, Pattern) |
| **Auto-Lock** | None (process kill only) | Configurable (1, 5, 15, 30 min, or never) |
| **Memory Cleanup** | Notes kept in RAM while backgrounded | Explicit zeroization — wiped from RAM on lock |
| **Device Security** | No enforcement | Mandatory screen lock — refuses to run if insecure |
| **Backup Strategy** | Zip-based (false confidence) | Honest device-binding — data stays on-device |

### Key Improvements Summary

**1. From Software to Hardware:** The "Root of Trust" moved from a software-derived password to the device's security chip (StrongBox/TEE). Even with full root access, an attacker cannot extract the encryption key.

**2. GPU Protection:** Argon2id makes brute-force attacks prohibitively expensive. Each password guess requires 64MB of dedicated RAM, rendering specialized hacking hardware useless.

**3. RAM Forensics Mitigation:** V1 kept decrypted notes and encryption keys in phone RAM — a memory dump during background state could reveal everything. V2 explicitly zeroizes this memory when the app locks.

**4. Enforced Hygiene:** The app refuses to run unless the user has a system PIN or pattern, ensuring StrongBox/TEE has a secure foundation.

---

## FEATURES RETAINED FROM EASYNOTES

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Markdown support with image attachments                                │
│  Home screen widgets                                                    │
│  Pinned notes                                                           │
│  Sort by date                                                           │
│  Dark / light / AMOLED themes with Material You dynamic colour         │
│  Per-note font size and monospace font option                           │
│  Multi-language support                                                 │
│  Minimalistic mode                                                      │
│  Swipe to edit / view toggle                                            │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## BUILDING

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Requires Android Studio with Gradle                                    │
│                                                                         │
│  Open project -> select fdroidDebug or fdroidRelease build variant      │
│                                                                         │
│  Signed release APK:                                                    │
│    Build -> Generate Signed App Bundle / APK -> APK                    │
│    Create or select keystore -> select fdroidRelease                   │
│                                                                         │
│  Minimum SDK   Android 8.0  (API 26)                                   │
│  Target SDK    Android 16   (API 36)                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## CREDITS

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Original app: EasyNotes by Kin69                                       │
│  https://github.com/Kin69/EasyNotes                                    │
│                                                                         │
│  Original contributors, translators and supporters are listed in the   │
│  upstream repository. This fork would not exist without their work.    │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## LICENSE

```
┌─────────────────────────────────────────────────────────────────────────┐
│  EasyNotes (original)                                                   │
│  Copyright (c) 2026 kin69                                               │
│                                                                         │
│  Notes - JEGLY 2026                                                     │
│  Modifications copyright (c) 2026 the respective contributors          │
│                                                                         │
│  This program is free software: you can redistribute it and/or modify  │
│  it under the terms of the GNU General Public License as published by  │
│  the Free Software Foundation, either version 3 of the License, or    │
│  (at your option) any later version.                                   │
│                                                                         │
│  This program is distributed in the hope that it will be useful,      │
│  but WITHOUT ANY WARRANTY; without even the implied warranty of        │
│  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                  │
│                                                                         │
│  https://www.gnu.org/licenses/                                         │
└─────────────────────────────────────────────────────────────────────────┘
```
