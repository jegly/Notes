<p align="center">
  <img src="notes_banner.png" alt="Notes Banner" width="800" />
</p>

> A private notes app for Android.

---


Your notes are encrypted with AES-256-GCM. The key lives in your device's secure hardware — TEE or StrongBox, depending on your chip. It never touches the JVM heap. When you uninstall the app, the key is gone. There is no recovery path, because there is no key to recover.

---

## What changed in this fork

**Removed**
- Cloud and NextCloud sync
- Vault mode toggle and vault lock UI
- Donation prompts, support links, Discord, Instagram
- Gallery sync
- Tools section / note count display
- Backup and restore — keys are device-bound and non-exportable, so backup was always broken by design

**Rebuilt**
- Password hashing: SHA-256 → Argon2id (64 MB memory, 3 iterations, OWASP 2024 recommended)
- Encryption key: app-managed → Android Keystore, hardware-backed
- Authentication: password-only → biometric unlock via `BiometricPrompt`, no password exposure
- Widget auth bypass: fixed (confirmed Android 16 / Pixel Fold)

**Added**
- Device screen lock enforced on launch — app refuses to run without PIN/pattern/biometric set
- Auto-lock timeout — configurable: 1, 5, 15, 30 minutes, or never
- In-memory zeroization on lock — decrypted note content is cleared from RAM when the app locks
- Export / Import plain text — replaces the defunct backup system
  

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

---




## Upstream

Fork of [EasyNotes by Kin69](https://github.com/Kin69/EasyNotes). Changes are intentionally not upstreamed — the threat model diverges too far from the original project's goals.

![GitHub all releases](https://img.shields.io/github/downloads/jegly/Notes/total)

