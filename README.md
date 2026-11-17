# Meta Patches

An independent Morphe patch source for the MetaConfig override payload.

<!-- PATCHES_START EXPANDED -->
> **[v1.1.0-dev.5](https://github.com/hemk651/meta-patches/releases/tag/v1.1.0-dev.5)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;10 patches total
<details open>
<summary>📦 Instagram&nbsp;&nbsp;•&nbsp;&nbsp;7 patches</summary>
<br>

**Package name:** `com.instagram.android`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [AMOLED theme](#amoled-theme) | Forces Instagram dark theme surfaces to pure black (AMOLED). |  |
| [Add Mods menu](#add-mods-menu) | Adds the Mods menu (fork of the InstaFL menu): long press the home feed to open the quick menu, incl. developer options. |  |
| [Change channel name](#change-channel-name) | Replaces the release channel name (NONE, ALPHA, BETA, PROD) shown in the developer options UI. | • Channel name |
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Packages the Employee Options MetaConfig override (28538::0 = true) and SAF installer for Instagram. Actual activation requires the app to read the deployed file; bytecode unlocking is not included. |  |
| [Remove ads](#remove-ads) | Removes ads in Instagram by stopping the ad pod flow. |  |
| [Remove snooze warning](#remove-snooze-warning) | Removes the "build expired" snooze warning dialog that alpha builds show. |  |
| [Unlock developer options](#unlock-developer-options) | Unlocks the developer options: writes the MobileConfig overrides file that reveals Developer options in the settings together with a dedicated Employee options entry, and forces the employee options gate to true (shake-gesture entry). The overrides file becomes active after an app restart. |  |

</details>

<details open>
<summary>📦 Edits&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.basel`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Packages the MetaConfig override and SAF installer for Edits. Employee flag compatibility and app-side loading are unverified; no bytecode unlock hook is included. |  |

</details>

<details open>
<summary>📦 Instants&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.moonshot`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Packages the MetaConfig override and SAF installer for Instants. Package and employee-flag compatibility require on-device verification; no bytecode unlock hook is included. |  |

</details>

<details open>
<summary>📦 Threads&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.barcelona`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Packages the MetaConfig override and SAF installer for Threads. Employee flag compatibility and app-side loading are unverified; no bytecode unlock hook is included. |  |

</details>

<!-- PATCHES_END -->

## Method 1 implementation notes

See [Method 1 — Employee Options MobileConfig override](docs/method-1-employee-flag.md) for the flag mapping, current implementation boundary, and verification sequence. The JSON payload is preserved unchanged. For Instagram the startup hook is now wired and verified against Instagram 457.0.0.0.26 in the build harness (see Runtime install below); for the other apps an APK-verified hook is still pending.

## Patch name

**Developer Options Unlocker (Employee Options)**

The same patch is declared separately for Instagram, Threads, Edits, and Instants.

## Installer implementation status

- The canonical JSON is bundled as an extension asset.
- A SAF installer helper is bundled with each app-specific patch.
- The extension bundles the canonical `mc_overrides.json` asset.
- A user-visible SAF setup Activity asks the user to choose the parent folder once; the installer persists that folder grant, creates `mobileconfig`, writes the JSON, and reads it back to verify exact bytes.
- The installer exposes a bounded `onAppLaunch(Context)` entry point, with a maximum of three write attempts after consent.
- **The app-specific startup hook is not yet wired.** The setup Activity and installer are present in the extension, but the four patch declarations still only package the extension. They do not yet invoke setup or `onAppLaunch` automatically.
- Note: the patcher merges only the extension's dex into the target APK (`.mpe` is a dex), so the extension's asset copy is not present in the target app. The SAF installer reads the payload from the target app's assets, so that flow still needs the asset shipped separately.

## Runtime install (Instagram)

For Instagram the payload is installed without any user consent flow:

- `config/mc_overrides.json` stays the canonical, validated source of truth and must remain a **single line** (Instagram's loader only reliably parses compact single-line JSON).
- The payload is embedded into the extension at build time as a `BuildConfig` string (`extensions/extension/build.gradle.kts`); no asset is required because the patcher merges only the extension's dex into the target APK.
- The "Unlock developer options" patch injects a call to `MCOverrides.install(Context)` right after the app shell's `Application.onCreate` super call, so it works without the optional "Add Mods menu" patch.
- The writer installs the payload atomically and idempotently to `<filesDir>/mobileconfig/mc_overrides.json` — the exact path Instagram's MobileConfig loader reads natively (verified in the app's smali). The settings become active on the next app start.
- The same patch also forces the employee options gate (`A00(UserSession)Z`) to true, which unlocks the shake-gesture developer options entry.
- Verified by `:patches:instagramApkBuild`: all patches apply, all DEX files pass dexdump, and the cross-DEX hierarchy issue set is identical to the stock APK.

## Storage access

The intended output is `/storage/<volume-id>/data/files/mobileconfig/mc_overrides.json`. Android's Storage Access Framework requires a user-selected folder grant; a volume UUID alone does not grant access. The user should select the intended `data/files` parent folder where Android's picker permits it. The helper creates the `mobileconfig` child directory.

Android 11+ may prohibit selecting storage roots or restricted directories. If the system picker does not allow the intended parent folder, this exact destination cannot be written through SAF on that device. The installer reports failure rather than silently writing somewhere else.

## Targets

| App | Package |
|---|---|
| Instagram | `com.instagram.android` |
| Threads | `com.instagram.barcelona` |
| Edits | `com.instagram.basel` |
| Instants | `com.instagram.moonshot` (candidate; verify on-device) |

## Validation

Run `python3 scripts/validate_config.py` to validate the source payload. This validates repository data only; it does not prove device deployment.

## Current limitation

For Instagram the startup hook is wired (see Runtime install above). The other apps still need an app-specific bytecode fingerprint and a verified call site; do not guess the fingerprint: inspect the exact APK for each app before enabling the hook. The SAF setup screen must be launched from a visible Activity context; Android does not permit silently granting access to arbitrary removable-storage folders. No app is currently advertised as end-to-end tested on device.
