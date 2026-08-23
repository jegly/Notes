<p align="center">
  <img src="notes_banner.png" alt="Notes Banner" width="800" />
</p>

> A private notes app for Android.

---


Your notes are encrypted with AES-256-GCM. The key lives in your device's secure hardware — TEE or StrongBox, depending on your chip. It never touches the JVM heap. When you uninstall the app, the key is gone, and there is no recovery path for it, because there is no key to recover.

That key never leaves the device, so it cannot move to a new phone with you. If you want your notes to survive a reinstall or a new device, take an encrypted backup first — it is protected by a passphrase you choose rather than by the hardware key, which is precisely what makes it portable.

---

## What changed in this fork

**Removed**
- Cloud and NextCloud sync
- Vault mode toggle and vault lock UI
- Donation prompts, support links, Discord, Instagram
- Gallery sync toggle — the image cleanup itself still runs, deleting the app's own cached
  copies when the corresponding gallery item goes away. It reads no image content.
- Tools section / note count display
- The home-screen widget — removed entirely in 2.3.0, along with its Glance dependencies

**Rebuilt**
- Password hashing: SHA-256 → Argon2id (64 MB memory, 3 iterations, OWASP 2024 recommended)
- Encryption key: app-managed → Android Keystore, hardware-backed
- Authentication: password-only → biometric unlock via `BiometricPrompt`, no password exposure

**Added**
- Device screen lock enforced on launch — app refuses to run without PIN/pattern/biometric set
- Auto-lock timeout — configurable: 1, 5, 15, 30 minutes, or never
- In-memory zeroization on lock — decrypted note content is cleared from RAM when the app locks
- Encrypted backup — a passphrase-protected export (Argon2id + AES-256-GCM) that *is*
  restorable on another device, deliberately independent of the device-bound note key
- Export / Import plain text
- Autosave — notes are written as you type, not only when the editor closes
  

---

## Security properties

| Property | Implementation |
|---|---|
| Encryption | AES-256-GCM, per-note |
| Key storage | Android Keystore (TEE / StrongBox) |
| Key lifetime | Device-bound; destroyed on uninstall |
| Password hashing | Argon2id — 64 MB, 3 iterations |
| Biometric unlock | `BiometricPrompt` — unlocks Keystore key, never touches password |
| Screen lock requirement | Enforced at runtime |
| Memory on lock | Zeroized |
| Key usability | Restricted to an unlocked device (`setUnlockedDeviceRequired`) |
| Backup | Separate passphrase key — Argon2id + AES-256-GCM, portable across devices |
| Network access | None. The app declares no `INTERNET` permission |

---




## Upstream

Fork of [EasyNotes by Kin69](https://github.com/Kin69/EasyNotes). Changes are intentionally not upstreamed — the threat model diverges too far from the original project's goals.

![GitHub all releases](https://img.shields.io/github/downloads/jegly/Notes/total)

