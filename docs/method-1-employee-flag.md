# Method 1 — Employee Options MobileConfig override

## Current payload

The canonical file is `config/mc_overrides.json`. It is intentionally not
rewritten by this method or by CI. It currently contains the candidate
Employee Options override:

- Universal ID: `28538`
- Sub-flag: `0`
- Encoded JSON entry: `"28538:": ["0: : true"]`
- Interpreted candidate: `28538::0 = true`

The identifier was located in the upstream Rex IG Lite flag-hook source:
https://github.com/xboyrex/rex-ig-lite/blob/main/extensions/instagram/src/main/java/app/morphe/extension/instagram/patches/devFlags/HookFlags.java

This is evidence that the upstream project knows this flag identifier. It is
not proof that an external `mc_overrides.json` file is read by every Meta app,
or that the flag alone exposes the Employee Options UI.

## Current implementation boundary

The Morphe entries package `extensions/extension.mpe`. The extension contains
a SAF-based installer and a visible folder-picker Activity. Android requires
the user to grant access to the target directory.

**The app-startup integration is not implemented.** None of the four app patch
declarations currently injects a call to `MetaConfigInstaller.onAppLaunch`,
and there is no APK-verified MobileConfig reader hook in this repository.
Therefore, a successful Morphe patch installation or successful file copy
must not be described as a confirmed Employee Options unlock.

## Test sequence

1. Apply the Instagram patch to an Instagram APK.
2. Launch the visible setup Activity through a verified integration point.
3. Grant write access to the intended removable-storage `data/files` folder.
4. Confirm that `mobileconfig/mc_overrides.json` is created.
5. Read the file back and compare exact bytes with the bundled asset.
6. Fully stop and restart Instagram.
7. Check whether Employee Options appears and record the exact app version.
8. If the file exists but the UI remains unavailable, Method 1 has not unlocked
   the feature. Proceed to an APK-specific MobileConfig hook / unlocker only
   after inspecting that app's DEX and validating method fingerprints.

## App compatibility

The four package registrations are discovery/packaging entries, not proof of
shared MobileConfig semantics. The flag must be tested separately in Instagram,
Threads, Edits, and Instants. The Instants package name is especially treated
as a candidate until verified on-device.

## CI

Run:

```sh
python3 scripts/validate_config.py
cmp config/mc_overrides.json extensions/extension/src/main/assets/mc_overrides.json
```

CI copies the canonical JSON into the extension asset directory before the
build. It does not edit or commit either source JSON file.
