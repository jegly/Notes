
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
\/     Y                               Y                        \/
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

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│   APP OPENS                                                             │
│     └─> Login screen                                                    │
│           └─> Password verified against stored PBKDF2 hash             │
│                 └─> PBKDF2-HMAC-SHA256 / 120,000 iterations            │
│                       └─> AES-256-GCM master key derived               │
│                             └─> Key cached in memory for session        │
│                                   └─> Notes decrypted on the fly       │
│                                                                         │
│   EVERY NOTE WRITE                                                      │
│     └─> AES-256-GCM                                                     │
│           └─> Fresh random 96-bit nonce per write                      │
│                 └─> GCM authentication tag detects tampering            │
│                                                                         │
│   BACKUP EXPORT                                                         │
│     └─> Zip encrypted with session password (AES-256-CBC, PBKDF2)     │
│           └─> Notes inside DB already AES-256-GCM encrypted            │
│                 └─> Double protected                                    │
│                                                                         │
│   RESTORE ON FRESH DEVICE                                               │
│     └─> Enter password -> decrypts zip                                  │
│           └─> Same password decrypts notes in restored DB              │
│                 └─> Different password? Notes re-encrypted to current  │
│                                                                         │
│   APP LOCK / PROCESS KILL                                               │
│     └─> Master key wiped from memory                                   │
│           └─> DB contains only ciphertext                              │
│                 └─> Login required on next open                        │
│          
│ sha256:fac5572eb27f9110929a09deb6786ea0810c35c1fe006b016ae7424cfaa13339
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  v2.0 — SHIPPED                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│  Android Keystore (TEE/StrongBox) — key never in JVM heap              │
│  Argon2id key derivation — memory-hard, GPU-resistant                  │
│  Device screen lock enforced — app refuses to run without it           │
│  Auto-lock after configurable timeout (1/5/15/30/never)                │
│  In-memory zeroization of decrypted content on lock                    │
│  Backup removed — device-bound keys make backup unrestorable           │
│  Export/Import as plain text for device migration                      │
└─────────────────────────────────────────────────────────────────────────┘
```

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
