# Release Notes — v2.3.1 (versionCode 25)

Compiled on this machine — `:app:compileDebugKotlin` passes against the project's own
Gradle 9.7.1 and JBR 21 toolchain. **Not run on a device.** Nothing below has been
exercised against real hardware, and the behaviour this release fixes is specific to the
secure element, so on-device verification matters more than usual here.

A point release. One user-visible theme: pasting a very large note made the app unusable,
and once that note was in the database it stayed unusable.

## Large notes made the whole app unresponsive

Reported after pasting a note well past 10,000 lines with biometric unlock enabled. The
app showed "Notes isn't responding" during the paste, again at unlock, and then on every
subsequent action — including creating an unrelated new note. Left alone it did eventually
recover, which is the tell: this was ANR, not deadlock.

Nothing here is a single bug. It is one architectural decision — the whole note body goes
through the secure element as one unit — meeting four places that assumed notes are small.

**Decryption ran on the main thread.** `ApplicationModule.provideCoroutineScope()` supplies
a `Dispatchers.Main` scope, and that is the scope `NoteUseCase.observe()` decrypts in. Every
note, in full, on the UI thread. `EditViewModel.setupNoteData()` had the same problem
independently: it decrypts inside `viewModelScope`, which is also main-dispatched, so simply
opening a large note froze the UI for as long as the secure element took to work through it.
Both now run on `Dispatchers.Default`.

**Every write re-decrypted every note.** Room re-emits the entire table whenever any row
changes, and `observe()` decrypted the lot each time. With one large note present, the cost
of decrypting it was charged to every save anywhere in the app — which is why creating a
brand new note hung. `NoteUseCase` now caches decrypted content keyed on note id and
validates it against the stored ciphertext; since ciphertext is randomised per encryption,
an unchanged ciphertext string is a reliable "this row was not rewritten" signal. The cache
holds plaintext, so `zeroize()` clears it on lock.

**Unchanged notes were re-encrypted anyway.** `persist()` wrote unconditionally, so every
autosave tick re-encrypted the full body whether or not anything had changed — and opening
a note and backing out without editing rewrote it. It now fingerprints what it last wrote
(hashes and lengths, not the strings, to avoid retaining a second copy) and skips redundant
writes. The fingerprint is seeded when a note loads, so a read-only visit writes nothing.

**Undo history was unbounded.** `UndoRedoState.onInput()` pushed a complete copy of the
field's text on every input event and nothing was ever dropped. At roughly 500 KB per
snapshot, a hundred keystrokes retained around 50 MB — this was the actual crash. History
is now bounded by depth (50) *and* total characters (~1M, about 2 MB of UTF-16); a count
limit alone is not enough when a single entry can be half a megabyte. Selection-only
changes no longer record a snapshot. On a note large enough to exceed the budget outright,
history collapses to the current state and undo becomes a no-op — bounded degradation
rather than an OOM.

## Markdown rendering

- **The parse re-ran on every recomposition.** `MarkdownText` built its element list inline
  with no `remember`, so every keystroke while editing re-parsed the entire note.
- **Note cards parsed the whole note to show four lines.** The preview path parsed every
  line and then took `content.take(4)`. It now reads at most 24 lines before parsing.
- **`buildString()` gained a fast path.** The general path allocates an ICU `BreakIterator`
  per line and, for every word, linearly scans all delimiter segments — quadratic in line
  length. Lines containing none of `*`, `=`, `~`, `_`, `` ` `` now skip it entirely.
- The line-processor list is built once rather than per call; `LinkProcessor` was compiling
  a `Regex` in its constructor every time.

## Other bug fixes

- **Undo could overwrite your note with its title.** `updateNoteName()` and
  `updateNoteDescription()` pushed into the same `UndoRedoState`, but `undo()` only writes
  back to `_noteDescription`. Typing in the title and then hitting undo replaced the note
  body with the title text. Each field now has its own history.
  (`screens/edit/model/EditModel.kt`)

## Known follow-ups

- **Note body still passes through the secure element byte for byte.** The proper fix is
  envelope encryption: bulk-encrypt the body with a random per-note data key in-process,
  and put only that 32-byte key through the Keystore. Same security posture, and the secure
  element handles 32 bytes instead of half a megabyte. It is a stored-format change and
  needs a migration, so it is deliberately not in this release.
- **The 2 MB `CursorWindow` row limit is unguarded.** Base64 inflates ciphertext by roughly
  1.37×, so a note past about 1.4 MB of text will fail to load with no useful error.
- Everything still open from 2.3.0 below, in particular release builds signing with the
  debug config while `SignatureVerifier` pins a release certificate hash.

## Verification status

Compiles. Not run. Recommended before publishing:

1. Unlock with biometrics with the large note already in the database — should reach the
   note list without an ANR dialog.
2. Create a new, unrelated note while that large note exists. This is the case that was
   reported broken.
3. Open the large note and back out without editing — should be immediate, and should not
   write to the database.
4. Type in a large note for a sustained period, then exercise undo and redo.
5. Confirm the title/body undo separation: type a title, edit the body, undo.

---

# Release Notes — v2.3.0 (versionCode 24)

Prepared on this machine; **not built or run**. There is no JDK here, so nothing below
has been compiled or exercised on a device — the reference sweeps and geometry checks
described are static. Build and test before signing and publishing. Network access to
Google Maven, Maven Central and jitpack was available, so dependency versions below were
resolved from live `maven-metadata.xml` rather than assumed.

## Crash fix — saving a note killed the app and lost the note

Reported on a Pixel 11 Pro Fold after upgrading from a Pixel 10 Pro Fold, both on
Android 17, with biometric unlock enabled. Three separate defects had to line up:

1. **The note was only ever written at `ON_STOP`.** The Save button called
   `onClickBack()` and nothing else — it navigated, it did not persist.
   (`screens/edit/EditScreen.kt`)
2. **The master key is generated with `setUnlockedDeviceRequired(true)`,** so the secure
   element refuses to use it once the device locks. With biometric unlock the device
   locks the instant the screen goes off — which is exactly when `ON_STOP` fires.
   (`security/KeystoreManager.kt`)
3. **`EncryptionHelper.encrypt()` ended in `.getOrThrow()`** inside a bare
   `viewModelScope.launch` with no handler, so that refusal escaped as an uncaught
   exception and took the process down. (`components/EncryptionHelper.kt`)

Why the old phone was fine: `generateKey()`'s fallback spec silently dropped **both**
StrongBox *and* `setUnlockedDeviceRequired`, so two devices running identical code ended
up with different key policies by accident of hardware, and only the strict one hit the
bug.

Fixes:

- `EncryptionHelper.encrypt()` returns `Result<String>` and never throws. `NoteUseCase`'s
  `encryptNote`/`addNote`/`reEncryptAllNotes` propagate it.
- **Debounced autosave (750 ms)** in `EditViewModel`, serialised behind a `Mutex` so an
  autosave and an explicit save cannot double-insert a new note. Encryption moved to
  `Dispatchers.IO` — StrongBox is slow and this was running on the main thread.
- Lifecycle save moved **`ON_STOP` → `ON_PAUSE`**, which runs before the device is
  considered locked. It is now a backstop; the autosave does the real work.
- **The Save button actually saves**, committing while the app is foregrounded and
  unlocked, then navigating.
- Save failures surface as a toast and leave the text in the editor to retry, instead of
  being dropped.
- `generateKey()` now walks an explicit ladder (StrongBox+unlocked → TEE+unlocked → TEE)
  keeping `setUnlockedDeviceRequired` on **both** hardware-backed tiers, so devices no
  longer get different security postures by accident. It refuses to run when a usable key
  already exists — the ladder deletes the alias between attempts and would otherwise
  destroy every note.
- `keyExists()` checks the key is actually *retrievable* rather than calling
  `containsAlias()`. A half-created StrongBox alias previously counted as "present", so
  the key was never regenerated and every save failed permanently.
- First-time setup no longer navigates into the app when key generation fails. It
  previously keyed navigation off the `isReEncrypting` flag, which clears on success and
  failure alike, landing users on the note list with no session and no key.

## Other bug fixes

- **Settings silently reset on every release rebuild.** `SettingsUseCase` persists each
  field using `field.name` as the DataStore key, but nothing kept those names — R8 renamed
  them, so keys shifted between builds. That includes `passwordHash`, which `isSetup` is
  derived from. Added a `-keepclassmembers` rule for `Settings`. (`proguard-rules.pro`)
- **Caret jumped to end of note while typing.** `setupNoteData` collected the note Flow
  continuously, so every write re-emitted the row and reset the text field. Now a one-shot
  load. (`screens/edit/model/EditModel.kt`)
- **Duplicate notes / wrong note adopted after first save.** Removed the `getLastNoteId()`
  round trip, which took the highest id in the table; the insert's row id is used directly.
  Also removes a non-suspend Room query that ran on the calling thread.
- **Exports could silently omit notes.** `notes` holds only successfully *decrypted* notes,
  and both export paths iterated it — a locked session or one undecryptable note produced a
  short, or empty, file reported as a success. Exports now refuse unless the decrypted count
  matches the row count. (`SettingsModel.kt`, `NoteUseCase.rawNoteCount()`)
- **Imports counted failures as successes** once `addNote` began returning `Result` rather
  than throwing. (`ImportExportUseCase.kt`)
- **Restore no longer claims success** when notes were dropped; it reports how many landed.
- **Three `Settings` fields could never load.** `viewMode`, `automaticTheme` and `darkTheme`
  were `val`, and the loader writes them by reflection. Now `var`, with per-field
  `runCatching` so one bad field cannot crash `SettingsViewModel.init`.
- **`FLAG_SECURE` and the MediaStore observer were applied in the composable body** — re-run
  on every recomposition and never undone, so turning screen protection off left the flag
  set. Both are now `DisposableEffect`. `unregisterGalleryObserver()` existed with zero
  callers. (`MainActivity.kt`)
- Nested `runBlocking(Dispatchers.IO)` inside a suspend function replaced with
  `withContext` — it was parking the main thread for the whole DataStore read.

## Removed

- **The home-screen widget is gone.** It never worked. Removed the `widget` package, the
  Glance dependencies, both receivers and the configure activity from the manifest, the
  widget XML resources, the ProGuard rule, `markdown/WidgetText.kt`, and all the plumbing
  it had spread through `MainActivity`, `NavHost`, `LoginScreen`, `SettingsModel` and both
  `SettingsRepository` files (`pendingWidgetNoteId`, `isWidget`, `getEveryNotesWidget`,
  `isAppLocked`, the dead `WidgetCoroutineScope`). A reference sweep comes back clean.

## New app icon

Three overlapping smiley faces — sad, flat, happy — replacing the previous mark. Shipped
as vector `foreground` and `monochrome` layers wired into `mipmap-anydpi-v26`, with the
background colour changed to `#1E1B14`. The artwork is the approved concept verbatim, its
native 110 tile mapped onto the icon's visible viewport, with the inter-face gaps as true
transparent cut-outs so the background layer shows through. Content sits inside the 66dp
safe circle — note that 66dp, not 72dp, is what every mask is guaranteed to show, which
matters now that Android 16+ ships cookie and arch masks.

## Dependencies

Resolved from live repository metadata. AGP 9.2.1 → **9.3.1**, Kotlin 2.3.21 → **2.4.10**,
KSP 2.3.9 → **2.3.11**, Hilt 2.59.2 → **2.60.1**, androidx.hilt 1.3.0/1.2.0 → **1.4.0**,
lifecycle-runtime-compose 2.8.7 → **2.11.0**, Coil 3.4.0 → **3.5.0**, core-ktx 1.16.0 →
**1.19.0**, appcompat 1.7.1 → **1.8.0**, core-splashscreen 1.0.1 → **1.2.0**, m3color
2024.6 → **2026.1**, biometric-ktx → **1.4.0-alpha02** (no stable line has ever shipped),
Gradle 9.6.1 → **9.7.1** with its SHA-256 now pinned in the wrapper properties.

Already current, left alone: Room 2.8.4, material3 1.4.0, navigation-compose 2.9.8,
activity-compose 1.13.0, datastore-preferences 1.2.1, argon2kt 1.6.0.
`material-icons-extended` stays at 1.7.8 — that artifact is frozen upstream.

## Compatibility

- **`compileSdk` 36 → 37.** `targetSdk` was already 37, which opted the app into every
  Android 17 behaviour change without compiling against the API defining them. The
  `buildToolsVersion = "36.0.0"` pin was removed so AGP selects a matching toolchain.
- **Foldable configuration changes handled in-process.** `MainActivity` now declares
  `configChanges` for the fold-relevant set. Without it the activity was destroyed and
  recreated on every fold, and as of Android 17 the IME is no longer restored afterwards —
  so the keyboard vanished mid-edit. `uiMode` is deliberately excluded so dark-mode changes
  still recreate normally.
- Checked and **not** affected: Android 17 makes static final fields immutable at
  targetSdk 37, but `SettingsUseCase`'s reflection filters statics out.
- `local.properties` pointed at a non-existent SDK path and was corrected.

## Verification status

Not compiled. Recommended before publishing:

1. `./gradlew assembleDebug` — this release carries a crash fix, a dependency upgrade, an
   SDK bump, a package removal and new icon assets together. Build-tools for API 37 may
   need installing via `sdkmanager`.
2. Create a note, lock the phone mid-edit, unlock — the note should be there and the app
   should still be running.
3. Fold/unfold while editing with the keyboard up.
4. Check the icon in Android Studio's adaptive icon preview across all mask shapes, and
   with themed icons enabled.

## Known follow-ups

- **No Gradle dependency verification**, and `m3color` still resolves from jitpack
  unsigned. Generate with `./gradlew --write-verification-metadata sha256 help` on the
  first successful build.
- **Release builds still sign with the debug config** while `SignatureVerifier` pins a
  release certificate hash, so a release build from this tree fails its own tamper check
  and refuses to launch.
- `ic_launcher-playstore.png` still shows the old icon; it is not generated from the
  adaptive layers.
- Four `.webp` icons per dpi bucket are now unreachable — at `minSdk` 33 `anydpi-v26`
  always wins.
- `SettingsViewModel.init` still blocks the main thread via `runBlocking`. Fixing it
  properly needs a startup loading state.

---

# Release Notes — v2.2.0 (versionCode 23)

Prepared on this machine; not yet built or run. **Build and test on the machine with
network/SDK access before signing and publishing** — this environment has no network
access, so none of the changes below have been compiled or exercised on a device.

## Security fixes

- **Imported notes were saved unencrypted.** `ImportExportUseCase.importNotes()` wrote
  directly through `NoteRepositoryImpl`, bypassing `NoteUseCase.addNote()` and the
  Keystore encryption step every other note-write path goes through. Notes created via
  Import (Settings → Export/Import) landed in the database in plaintext. Fixed by
  routing import through `NoteUseCase.addNote()`, same as manual note creation.
  (`domain/usecase/ImportExportUseCase.kt`)

## Security hardening

- **Brute-force lockout on the app password.** After 5 failed attempts, further
  attempts are refused with an escalating cooldown (30s, 1m, 2m, ... capped at 30 min),
  persisted across app restarts. Login screen shows a live countdown.
  (`SettingsModel.kt`, `LoginScreen.kt`)
- **Screen protection (`FLAG_SECURE`) now defaults on**, blocking screenshots and the
  recent-apps thumbnail preview out of the box. Previously opt-in and off by default.
  (`Settings.kt`)
- **Minimum app password length raised 4 → 6 characters.** (`PasswordSetupScreen.kt`)
- **Home-screen widget lock check was dead code** — `isAppLocked()` never actually
  gated note content shown in the widget in the path that mattered. Now the widget
  checks whether a session is genuinely active (`EncryptionHelper.isPasswordEmpty()`)
  and fails closed (shows the locked placeholder) if the app process was killed.
  (`widget/NotesWidget.kt`)
- **"Lock now" action** added to Privacy settings — instantly zeroizes the session and
  returns to the login screen, without waiting for auto-lock.

## New features

- **Encrypted, device-independent backup.** Previously, backup/restore was removed
  entirely because note encryption keys are Android Keystore-bound (device-only).
  This adds a *separate* backup path: Settings → Export/Import → "Encrypted backup" /
  "Restore backup", protected by a user-chosen passphrase (Argon2id key derivation +
  AES-256-GCM, `.nbak` file format, magic header `NOTESBK2`). Restorable on any device,
  including after reinstall — independent of the Keystore-bound note encryption.
  (`security/BackupCrypto.kt`, `SettingsModel.kt`, `settings/Backup.kt`)
- **44 new color themes** ported from GNOME Ptyxis terminal palettes: 11 curated
  (Nord, Synthwave, C64, etc.) plus 33 extended (Kanagawa, Monokai Pro, Solarized,
  GitHub, Everforest, etc.), selectable in Settings → Color Styles alongside the
  existing Catppuccin/Dracula presets. (`theme/Ptyxis.kt`, `theme/PtyxisPalettesExtended.kt`)
- **Font picker** — 13 selectable UI font families (Nunito (default), Cormorant
  Garamond, IBM Plex Mono/Serif, Playfair Display, Space Grotesk, Quicksand, and
  others), each previewed in its own typeface in the picker. Settings → Color Styles →
  Font. (`theme/Fonts.kt`, `res/font/`)

## Compatibility

- **`minSdk` lowered 34 → 33**, restoring Android 13 support. No code changes were
  needed — all version-gated calls (StrongBox, hardware security level, dynamic color)
  were already guarded at API 31 (S) or lower; nothing in the app actually required
  API 34.

## Verification status

Manually reviewed, not compiled. This sandbox has no network access, so Gradle
cannot resolve dependencies here (confirmed both online and `--offline` — no cached
artifacts for this dependency set). On the build machine:

- [ ] `./gradlew assembleDebug` (or `assembleRelease`) succeeds
- [ ] App launches, login/password setup flow works
- [ ] New "Font" picker and Ptyxis theme entries render correctly in Settings → Color Styles
- [ ] Encrypted backup export, then restore on the same device, round-trips correctly
- [ ] Import (Settings → Export/Import → Import) produces notes that show as encrypted in the DB
- [ ] Lockout countdown appears after 5 wrong passwords and actually blocks retry
- [ ] Widget shows locked placeholder when app is locked, real content when unlocked
