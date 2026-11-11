# Implementation plan

## Scope

Exactly these four Android apps are in scope:

1. Instagram
2. Threads
3. Edits
4. Instant

The payload is shared. Package IDs, version fingerprints, startup integration, and storage access are app-specific.

## Phases

### Phase 1 — repository and payload
- [x] Create repository.
- [x] Add canonical `config/mc_overrides.json`.
- [x] Add JSON validation script.
- [x] Document Android removable-storage constraints.
- [ ] Confirm the package ID for Edits.
- [ ] Confirm the exact identity/package ID meant by Instant.

### Phase 2 — Morphe patch project
- [ ] Align project files with the current official Morphe patches template.
- [ ] Add app compatibility declarations for verified package IDs.
- [ ] Add a shared implementation component for the payload.
- [ ] Add app-specific fingerprints/startup hooks only after inspecting each target APK.

### Phase 3 — storage permission and write
- [ ] Implement a user-mediated Storage Access Framework flow if the destination is a shared removable-storage folder.
- [ ] Persist the granted tree URI.
- [ ] Resolve the target directory without hard-coded volume UUIDs.
- [ ] Write atomically and verify the resulting bytes.
- [ ] Provide clear failure reporting and rollback.

### Phase 4 — validation
- [ ] Build the Morphe patch bundle.
- [ ] Test each app separately on a clean install and an update.
- [ ] Verify app startup when storage permission is denied.
- [ ] Verify no app crash or unwanted network behavior.
- [ ] Publish only the app/version combinations that pass.

## Current blocker

A normal Android app cannot assume write access to an arbitrary path under `/storage/<volume-id>` on Android 16. The target directory must be made writable through a supported permission mechanism. Also, app-specific APK fingerprints cannot responsibly be invented without the target APK versions.
