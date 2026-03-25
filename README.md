# Notes

A private, security-hardened Android notes app with AES-256-GCM encryption for all notes.

This is a stripped-down, security-focused fork of [EasyNotes](https://github.com/Kin69/EasyNotes) by [Kin69](https://github.com/Kin69). The original app has a broader feature set including cloud sync, a donation system, community translations, and more — this fork removes those in favour of a minimal, private-use build with significantly hardened encryption and authentication.

---

## What's different from EasyNotes

**Removed**
- Cloud / NextCloud sync
- Fingerprint and pattern lock (cannot derive an AES key from biometrics)
- Separate vault mode / vault toggle / vault lock icon
- Support links, donation prompts, Discord, Instagram, feature request links
- Gallery sync setting
- Tools section (note count display)
- Unencrypted backup option

**Changed**
- App name and package ID (`com.notes.app`)
- All branding references to EasyNotes removed

**Added / hardened**
- Single unified password — one password locks the app and encrypts all notes
- All notes are AES-256-GCM encrypted at all times, no opt-in required
- PBKDF2-HMAC-SHA256 at 120,000 iterations for key derivation (was single-pass SHA-256)
- Master key derived once at login and cached in memory — no repeated PBKDF2 per note
- Passcode stored as a salted PBKDF2 hash (was stored as plaintext in DataStore)
- Backup always encrypted with session password, no separate prompt
- Restore prompts for the backup password separately — supports cross-device restores where backup and current password differ, and re-encrypts all notes to the current password after restore
- Widget auth bypass fixed (confirmed on Android 16 / Pixel Fold — widget tap now routes through login before opening any note)
- `getDefaultRoute()` fixed to check all auth methods, not just passcode
- `loadDefaultRoute()` typo fixed (`==` → `=`) that caused `defaultRoute` to never be set
- `data_extraction_rules.xml` populated — cloud backup and device-to-device transfer explicitly blocked for database, preferences, and images
- `FLAG_SECURE` applied before first frame renders (splash screen was previously unprotected)
- First-launch setup screen — app cannot be used without setting a password

---

## Security model

```
App opens
  → Login screen — password verified against stored PBKDF2 hash
  → PBKDF2-HMAC-SHA256 (120,000 iterations) derives AES-256-GCM master key
  → Master key cached in memory for session
  → All notes decrypted on the fly

Every note write
  → AES-256-GCM with fresh random 96-bit nonce
  → GCM authentication tag detects tampering

Backup export
  → Zip encrypted with session password (AES-256-CBC, PBKDF2 key)
  → Notes inside database are already AES-256-GCM encrypted
  → Double protected

Restore on fresh device
  → Enter password → decrypts zip
  → Same password decrypts notes inside restored database
  → If restoring from a different password, notes are re-encrypted to current password

App lock / process kill
  → Master key wiped from memory
  → Database contains only ciphertext
  → Login required on next open
```

**Planned for v2.0**
- Argon2id key derivation (memory-hard, GPU-resistant)
- Auto-lock after configurable timeout
- Clipboard auto-clear after copying note content
- In-memory zeroization of decrypted content on lock

---

## Features retained from EasyNotes

- Markdown support with image attachments
- Home screen widgets
- Pinned notes
- Sort by date
- Dark / light / AMOLED themes with Material You dynamic colour
- Per-note font size and monospace font option
- Multi-language support
- Minimalistic mode
- Swipe to edit / view toggle

---

## Building

Requires Android Studio with Gradle. Open the project, select the `fdroidDebug` or `fdroidRelease` build variant, and build.

For a signed release APK: **Build → Generate Signed App Bundle / APK → APK**, create or select your keystore, select `fdroidRelease`.

Minimum SDK: Android 8.0 (API 26)  
Target SDK: Android 16 (API 36)

---

## Credits

Original app: **EasyNotes** by [Kin69](https://github.com/Kin69/EasyNotes)  
Original contributors, translators and supporters are listed in the upstream repository.  
This fork would not exist without their work.

---

## License

    EasyNotes (original)
    Copyright (c) 2026 kin69

    Notes - JEGLY 2026
    Modifications copyright (c) 2026 the respective contributors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
