# Implementation plan

## Completed

- [x] Canonical JSON payload (`config/mc_overrides.json`) and repository validator (`scripts/validate_config.py`).
- [x] Morphe patch set ported from InstaFL for Instagram.
- [x] Runtime payload writer (`MCOverrides.install`) injected after `InstagramAppShell.onCreate`; writes `<filesDir>/mobileconfig/mc_overrides.json` atomically and idempotently.
- [x] Payload embedded into the extension at build time as a `BuildConfig` string from the canonical config file (the patcher merges only the extension's dex, so no asset is shipped).
- [x] Employee options gate forced true (bytecode), unlocking the shake-gesture developer options entry.
- [x] Verified against Instagram 457.0.0.0.26 in the build harness (`:patches:instagramApkBuild`): all patches apply, all DEX files pass dexdump, cross-DEX hierarchy issue set identical to the stock APK.
- [x] Confirmed working on device (v1.1.0-dev.5): payload installed and developer options unlocked without the optional Mods menu.
- [x] Full ad-removal override payload embedded into the extension (v1.1.0-dev.7).
- [x] Release bundle carries the on-device patcher dex (`classes.dex`) since v1.1.0-dev.8; a workflow-side guard that fails the release without it is prepared but pending a push token with the `workflow` scope.
- [x] Fixed the Mods menu crash on a home long press (v1.1.0-dev.9): the replaced `onLongClick` listener and `openDeveloperOptions` body now use InstaFL's fixed register layouts (`.registers 6` / `.registers 7`, dismiss through `p0`) instead of hardcoded registers on an uninitialized one; verified by dex inspection of the harness output APK.
- [x] Home sheet handler degrades gracefully: `try/catch(Throwable)` reports into the in-app crash reports (`<external files dir>/mp_update_files/mp_crashlogs.json`) and toasts `Menu error: <Exception>` instead of crashing the app.
- [x] `ModsInit.init` injection is guarded per call site (not "any extension reference"), so "Unlock developer options" and "Add Mods menu" no longer suppress each other's hook.
- [x] Outside-tap closing and graceful menu actions (v1.1.0-dev.10): tapping the dimmed background (or an empty spot) closes the quick sheet and the Mods menu activity like a back gesture; sheet items, menu tiles and both `onCreate` bodies are wrapped so failures are reported (`Menu error: <Exception>` toast + crash report) instead of crashing. The final APK's manifest was verified to declare all eight menu activities (`aapt2 dump xmltree`).
- [x] Root-caused the "Open Meta menu" crash with the on-device log (v1.1.0-dev.11): the injected activities declared no theme, so they inherited the app's splash theme chain (`Theme.Instagram.Splash`), whose `textAppearance` attributes fail to resolve — `new TextView()` threw `Failed to resolve attribute at index 7`. All eight menu activities now declare the self-contained framework theme `@android:style/Theme.Material.NoActionBar` in the manifest; the reference was verified in the output APK (`@0x0103022e`).

## Removed (dead machinery)

The original SAF/Storage Access Framework installer design was removed because it was architecturally dead: the patcher merges only the extension's dex into the target APK, so the extension's asset never reached the target app — and the SAF installer read `mc_overrides.json` from the *host* app's assets. The working path is the runtime writer above.

Removed: the four per-app packaging patch declarations (Instagram/Threads/Edits/Instants "Developer Options Unlocker (Employee Options)"), `MetaConfigInstaller`, `MetaConfigSetupActivity`, the extension asset copy, and the SAF-related documentation. The "Unlock developer options" patch (runtime writer + gate) is the supported path and stays.

## Still open

- [ ] Extend "Remove ads" to cover stories ads, promotional videos, and reels ads (currently a bytecode-only port of the InstaFL ad-pod patch).
- [ ] Confirm the Mods menu and "Open Meta menu" work on device with v1.1.0-dev.11 (theme fix verified at the manifest level only).
- [ ] Push the prepared release workflow guard (`classes.dex` presence check); requires `gh auth refresh -h github.com -s workflow`.
