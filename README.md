<p align="center">
  <img src="notes_banner.png" alt="Notes Banner" width="800" />
</p>

> A private notes app for Android.

---


Your notes are encrypted with AES-256-GCM. The master key lives in your device's secure hardware — TEE or StrongBox, depending on your chip — and never leaves it. Each note gets its own random data key, and the master key wraps that; only those 32 bytes ever pass through the secure element, which is what keeps large notes fast. A note's data key exists in app memory only while that note is being read or written, and is worthless without the hardware key. When you uninstall the app, the master key is gone, and there is no recovery path for it, because there is no key to recover.

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
- Autosave — notes are written as you type, not only when the editor closes, and skipped
  entirely when nothing has actually changed
  

---

## Large notes

Up to 2.3.0 a note's title and body each went through the secure element in full. That is
fine for a shopping list and pathological for anything big — StrongBox is built for small
operations, and a note of a few thousand lines took minutes to save and minutes to load.

2.3.1 switched to envelope encryption: the body is encrypted in process with a random
per-note data key, and only that 32-byte key goes through the secure element. Alongside
that, decryption runs off the main thread, notes are published to the list as each one
finishes rather than in a single batch at the end, decrypted content is cached against its
stored ciphertext so an unrelated save no longer re-decrypts everything, and undo history
is bounded by both depth and total size.

Notes written before 2.3.1 are still readable; they convert to the new format the next time
you save them. Nothing is rewritten behind your back.

One hard ceiling remains. Android caps a single database row at 2 MB, and Base64 inflates
stored ciphertext by roughly 1.37×, so a note beyond roughly 1.4 MB of text — on the order
of 30,000 lines — will fail to load. Split notes that approach that size.

---

## Security properties

| Property | Implementation |
|---|---|
| Encryption | AES-256-GCM, per-note |
| Key hierarchy | Envelope — a random per-note data key, wrapped by the hardware master key |
| Key storage | Android Keystore (TEE / StrongBox) — master key only, never extractable |
| Key lifetime | Device-bound; destroyed on uninstall |
| Data key exposure | In app memory only during a read or write of that note; unusable without the hardware key |
| Password hashing | Argon2id — 64 MB, 3 iterations |
| Biometric unlock | `BiometricPrompt` — unlocks Keystore key, never touches password |
| Screen lock requirement | Enforced at runtime |
| Memory on lock | Zeroized |
| Key usability | Restricted to an unlocked device (`setUnlockedDeviceRequired`) |
| Backup | Separate passphrase key — Argon2id + AES-256-GCM, portable across devices |
| Network access | None. The app declares no `INTERNET` permission |

---




## Release signing

Release APKs use hybrid signing (APK Signature Scheme v3.2): the original app key plus an EC P-384 key and an ML-DSA-87 post-quantum key. Android 17 and newer verify the hybrid block, where the ML-DSA-87 key is the signer; earlier Android versions verify the original key as before.

| File | SHA-256 |
| --- | --- |
| `Notes-2.3.4-PQC-signed.apk` | `aae184a48b1c7e820b21fefe350fa6df8dfd4e630b1ad38ed9f269dd02cce7e0` |

Verify a download:

```bash
sha256sum Notes-2.3.4-PQC-signed.apk
apksigner verify -v --print-certs Notes-2.3.4-PQC-signed.apk   # build-tools 37.0.0 on JDK 25 shows all three keys
```

Signing certificate SHA-256:

- ML-DSA-87 (post-quantum): `5f99de5b3195753d4fe8ddabebd2fb27356b2109d6fbb2471fcbbdf21a9e76a6`
- EC P-384 (hybrid classical): `fa8f4a07e6a4f2b7dc6e5ab31fc05592b1414ab9436d1e2e39b35ce3bb2e33a2`
- Original key: `98d324d4106a368c62729a0a24d9ac9a6b47f8ac4c6585348531f0ee4eb6a04c`

## Upstream

Fork of [EasyNotes by Kin69](https://github.com/Kin69/EasyNotes). Changes are intentionally not upstreamed — the threat model diverges too far from the original project's goals.

![GitHub all releases](https://img.shields.io/github/downloads/jegly/Notes/total)

