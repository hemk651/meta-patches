# Meta Patches

An independent Morphe patch source for the MetaConfig override payload.

<!-- PATCHES_START EXPANDED -->
> **[v1.1.0-dev.2](https://github.com/hemk651/meta-patches/releases/tag/v1.1.0-dev.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 Edits&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.basel`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Provides the user-consented MetaConfig installer for Edits. A user-granted folder permission is required; startup invocation is not yet wired. |  |

</details>

<details open>
<summary>📦 Instagram&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.android`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Provides the user-consented MetaConfig installer for Instagram. A user-granted folder permission is required; startup invocation is not yet wired. |  |

</details>

<details open>
<summary>📦 Instants&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.moonshot`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Provides the user-consented MetaConfig installer for Instants. A user-granted folder permission is required; startup invocation is not yet wired. |  |

</details>

<details open>
<summary>📦 Threads&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.barcelona`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Developer Options Unlocker (Employee Options)](#developer-options-unlocker-employee-options) | Provides the user-consented MetaConfig installer for Threads. A user-granted folder permission is required; startup invocation is not yet wired. |  |

</details>

<!-- PATCHES_END -->

## Method 1 implementation notes

See [Method 1 — Employee Options MobileConfig override](docs/method-1-employee-flag.md) for the flag mapping, current implementation boundary, and verification sequence. The JSON payload is preserved unchanged. The current repository does not yet contain an APK-verified startup or MobileConfig bytecode hook.

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

The startup hook needs an app-specific bytecode fingerprint and a verified call site. Do not guess this fingerprint: inspect the exact APK for each app before enabling the hook. The SAF setup screen must be launched from a visible Activity context; Android does not permit silently granting access to arbitrary removable-storage folders. No app is currently advertised as end-to-end tested.
