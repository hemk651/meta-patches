# Dynamic mobileconfig storage path

Requested destination:

```text
/storage/<volume-id>/data/files/mobileconfig/mc_overrides.json
```

The implementation does not hard-code a volume ID. It bundles the canonical JSON and uses a persisted Storage Access Framework tree URI.

## User permission flow

1. A visible Activity must launch `MetaConfigInstaller.createFolderPickerIntent()`.
2. The user selects the intended `data/files` parent folder (if the system picker exposes it).
3. Pass the returned URI and result flags to `MetaConfigInstaller.install(context, uri, flags)`.
4. The helper persists the URI grant, creates `mobileconfig`, writes `mc_overrides.json`, and reads it back to verify exact bytes.
5. A verified app-start hook may call `MetaConfigInstaller.onAppLaunch(context)`; this retries only for the first three launches.

The helper never opens an Activity from a background provider or silently falls back to app-private storage.

## Android 11+ limitation

Android restricts SAF selection of certain roots and directories, including some SD-card roots. If the intended parent cannot be selected, the exact requested path is not available through this mechanism on that device. A raw volume path is not proof of write permission.

## Status

The asset and installer helper are bundled. The target-app picker UI and per-app startup bytecode hooks are not yet wired. End-to-end deployment remains unverified.
