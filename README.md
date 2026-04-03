<p align="center">
<pre>
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
</pre>
</p>

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
│  Separate vault mode / vault toggle / vault lock icon                  │
│  Support links, donation prompts, Discord, Instagram, feature requests │
│  Gallery sync setting                                                   │
│  Tools section (note count display)                                     │
│  Backup / restore — keys are device-bound, backup is unrestorable      │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  CHANGED                                                                │
├─────────────────────────────────────────────────────────────────────────┤
│  App name and package ID (com.notes.app)                                │
│  All branding references to EasyNotes removed                           │
│  Backup section replaced with Export / Import plain text               │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  ADDED / HARDENED                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│  Android Keystore (TEE/StrongBox) — AES-256-GCM key generated inside   │
│    secure hardware, raw key bytes never enter JVM heap                 │
│  Argon2id password hashing — 64MB memory, 3 iterations (OWASP 2024)   │
│    replaces single-pass SHA-256 used in original                       │
│  Biometric unlock — fingerprint/face via BiometricPrompt, unlocks      │
│    Keystore key without exposing password                              │
│  Device screen lock enforced — app refuses to run without PIN/pattern  │
│  Auto-lock timeout — configurable 1/5/15/30/never minutes             │
│  In-memory zeroization on lock — decrypted content cleared from RAM    │
│  Uninstall = cryptographic destruction — Keystore key wiped by Android │
│  Widget auth bypass fixed (confirmed Android 16 / Pixel Fold)          │
│  getDefaultRoute() fixed to check all auth methods                     │
│  loadDefaultRoute() typo fixed (== -> =)                               │
│  data_extraction_rules.xml — cloud backup + device transfer blocked    │
│  FLAG_SECURE before first frame (splash was previously unprotected)    │
│  First-launch setup — app unusable without setting a password          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## SECURITY MODEL

```
┌─────────────────────────────────────────────────────────────────────────┐
│  APP OPENS                                                              │
│    └─> Device screen lock check — refuses if no PIN/pattern/password   │
│          └─> Password verified via Argon2id (64MB RAM, 3 iterations)   │
│                or biometric via BiometricPrompt                        │
│                  └─> Keystore key unlocked (stays in TEE/StrongBox)    │
│                        └─> Notes decrypted via hardware-backed Cipher  │
│                              └─> Raw key bytes never in JVM heap       │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  EVERY NOTE WRITE                                                       │
│    └─> AES-256-GCM via Keystore-backed Cipher                          │
│          └─> Fresh random 96-bit nonce per write                       │
│                └─> 128-bit GCM auth tag detects any tampering          │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  AUTO-LOCK                                                              │
│    └─> App backgrounds — countdown starts (user-configured timeout)    │
│          └─> Timer fires — session token zeroized                      │
│                └─> Notes list cleared from Compose state               │
│                      └─> Login required to resume                      │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  DEVICE MIGRATION                                                       │
│    └─> Export notes as plain text (warning shown before export)        │
│          └─> Install on new device, set new password                   │
│                └─> Import txt files — re-encrypted with new key        │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  UNINSTALL                                                              │
│    └─> Android deletes all Keystore keys for the app automatically     │
│          └─> Cryptographic destruction — notes permanently unreadable  │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  HARDWARE SECURITY LEVEL  (visible in Settings > Privacy and About)    │
│    StrongBox  — dedicated tamper-resistant secure element  (Pixel 3+)  │
│    TEE        — Trusted Execution Environment  (most other devices)    │
│    Software   — fallback only                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│  WHAT CANNOT BE DONE  (JVM platform limitations)                       │
│    mlock — JVM cannot lock memory pages to prevent swap to disk        │
│    True byte-level zeroization — JVM GC owns heap memory              │
│    These gaps affect every Android notes app including commercial ones │
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
│  Open project → select fdroidDebug or fdroidRelease build variant       │
│                                                                         │
│  Signed release APK:                                                    │
│    Build → Generate Signed App Bundle / APK → APK                      │
│    Create or select keystore → select fdroidRelease                    │
│                                                                         │
│  New dependency (downloads automatically on first Gradle sync):         │
│    com.lambdapioneer.argon2kt:argon2kt:1.5.0                           │
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
│  Notes — JEGLY 2026                                                     │
│  Modifications copyright (c) 2026 the respective contributors          │
│                                                                         │
│  GNU General Public License v3                                          │
│  https://www.gnu.org/licenses/                                         │
└─────────────────────────────────────────────────────────────────────────┘
```
