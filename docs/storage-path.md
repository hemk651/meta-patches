# Dynamic mobileconfig storage path

The requested output path follows this pattern:

```text
/storage/<volume-id>/data/files/mobileconfig/mc_overrides.json
```

Example only (the volume ID is not fixed):

```text
/storage/23DE-10EA/data/files/mobileconfig/mc_overrides.json
```

## Requirements

1. Never hard-code `23DE-10EA` or another volume ID.
2. Discover the currently mounted volume at runtime.
3. Create the `data/files/mobileconfig` directories if permitted.
4. Write the canonical bundled JSON as UTF-8.
5. Validate the JSON before writing.
6. Avoid overwriting a user-edited file without a backup or explicit policy.
7. Report permission denial clearly; do not claim success if the write failed.

## Android storage access

On modern Android, a normal app generally cannot write arbitrary paths at the root of removable storage due to scoped-storage restrictions. Discovering a StorageVolume does not itself grant write access.

The preferred implementation should use the Storage Access Framework: ask the user to select the intended removable-storage root (or the closest permitted parent), persist the returned tree URI permission, and create the required child directories using DocumentFile APIs.

If the target path cannot be represented or written through the granted tree, show a clear unsupported-permission result. Do not fall back to app-private storage because that would not satisfy the requested destination.

## Verification

A successful operation must verify that the destination document exists and its bytes match the canonical configuration. A failed permission request or failed write must be reported as a failure.
