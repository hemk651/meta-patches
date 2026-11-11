# Meta Patches

An independent Morphe patch source for the MetaConfig override payload.

<!-- PATCHES_START [EXPANDED] -->
<!-- PATCHES_END -->

## Patch name

**Developer options unlocker (Employee options)**

The same patch is declared separately for Instagram, Threads, Edits, and Instants.

## Installer implementation status

- The canonical JSON is bundled as an extension asset.
- A SAF installer helper is bundled with each app-specific patch.
- The helper creates `mobileconfig/mc_overrides.json` beneath a user-selected parent folder, verifies the written bytes, and persists the folder grant.
- A bounded `onAppLaunch(Context)` entry point retries installation on at most three launches after a grant exists.
- **The app-specific startup hook and permission-picker UI are not yet wired.** The patch must not be described as automatically installing the file until those pieces are implemented and tested against each target APK.

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

The startup hook needs an app-specific bytecode fingerprint. Do not guess this fingerprint: inspect the exact APK for each app before enabling the hook. No app is currently advertised as end-to-end tested.
