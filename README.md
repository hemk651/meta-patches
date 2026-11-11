# Meta Patches

A third-party Morphe patch source for selected Meta Android apps.

## Supported apps

The initial target list is intentionally limited to:

- Instagram
- Threads
- Edits
- Instant — package identity to be confirmed

Support is app-version-specific. A listed app is not considered compatible until its package identity, startup hook, storage behavior, and target build have been verified.

## Current feature

**Install MetaConfig overrides file** — provide the same bundled `mc_overrides.json` configuration for each supported app and create it in the app's requested mobileconfig directory.

Target directory pattern:

```text
/storage/<volume-id>/data/files/mobileconfig/mc_overrides.json
```

The volume identifier is dynamic. The implementation must discover mounted storage at runtime and must not hard-code a device-specific identifier.

Android scoped-storage restrictions mean an app cannot necessarily write to an arbitrary removable-storage root. The patch must use a supported user-granted Storage Access Framework tree URI or another verified access path. It must not silently write to a different directory.

## Configuration

The canonical configuration is [mc_overrides.json](config/mc_overrides.json).

## Development status

This repository is in its initial scaffolding stage. No app-specific patch is marked working until tested against the exact app package and version.

## Safety and account risk

MetaConfig experiments and server-side flags can be unstable and may carry account or service risk. This project does not guarantee that any override is safe, supported by Meta, or available to every account. Use test accounts where appropriate and keep backups.

## Build

The project follows the official [Morphe Patches template](https://github.com/MorpheApp/morphe-patches-template) and its build workflow. Build instructions will be finalized after the patch modules and supported app fingerprints are implemented.

## Disclaimer

This is an independent, community-made project and is not affiliated with, endorsed by, or sponsored by Meta or Morphe.
