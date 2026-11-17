# Implementation plan

## Completed

- [x] Canonical JSON payload (`config/mc_overrides.json`) and repository validator (`scripts/validate_config.py`).
- [x] Morphe patch set ported from InstaFL for Instagram.
- [x] Runtime payload writer (`MCOverrides.install`) injected after `InstagramAppShell.onCreate`; writes `<filesDir>/mobileconfig/mc_overrides.json` atomically and idempotently.
- [x] Payload embedded into the extension at build time as a `BuildConfig` string from the canonical config file (the patcher merges only the extension's dex, so no asset is shipped).
- [x] Employee options gate forced true (bytecode), unlocking the shake-gesture developer options entry.
- [x] Verified against Instagram 457.0.0.0.26 in the build harness (`:patches:instagramApkBuild`): all patches apply, all DEX files pass dexdump, cross-DEX hierarchy issue set identical to the stock APK.
- [x] Confirmed working on device (v1.1.0-dev.5): payload installed and developer options unlocked without the optional Mods menu.

## Removed (dead machinery)

The original SAF/Storage Access Framework installer design was removed because it was architecturally dead: the patcher merges only the extension's dex into the target APK, so the extension's asset never reached the target app — and the SAF installer read `mc_overrides.json` from the *host* app's assets. The working path is the runtime writer above.

Removed: the four per-app packaging patch declarations (Instagram/Threads/Edits/Instants "Developer Options Unlocker (Employee Options)"), `MetaConfigInstaller`, `MetaConfigSetupActivity`, the extension asset copy, and the SAF-related documentation. The "Unlock developer options" patch (runtime writer + gate) is the supported path and stays.

## Still open

- [ ] Extend "Remove ads" to cover stories ads, promotional videos, and reels ads (currently a bytecode-only port of the InstaFL ad-pod patch).
