
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
![GitHub all releases](https://img.shields.io/github/downloads/jegly/Notes/total)
## WHAT'S DIFFERENT FROM EASYNOTES
sha256:4977d9cb18e0e2060b67cfebdaba6e76e51bb7bacc3f79a70a114e39ab142a11
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

