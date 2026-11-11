# Implementation plan

## Scope

Exactly four apps are in scope: Instagram, Threads, Edits, and Instants. The configuration payload is shared; package compatibility, startup hooks, and storage access are app-specific.

## Phase 1 — repository foundation

- [x] Create the GitHub repository.
- [x] Add the canonical `config/mc_overrides.json`.
- [x] Add a JSON validation script.
- [x] Add initial Morphe Gradle configuration.
- [x] Document Android removable-storage constraints.
- [x] Identify Edits package ID as `com.instagram.basel`.
- [x] Identify Instants as the standalone app; candidate package ID is `com.instagram.moonshot`.
- [ ] Verify all package IDs against installed APKs.

## Phase 2 — Morphe patch implementation

- [x] Add initial Gradle plugin configuration based on the official Morphe template.
- [ ] Add the shared payload to the patched APK in a form accessible to runtime code.
- [ ] Implement app-specific compatibility declarations and version fingerprints.
- [ ] Implement startup hooks for each app only after inspecting the target APKs.

## Phase 3 — storage access and write

- [ ] Confirm whether the target folder is accessible directly by each app.
- [ ] If required, implement a user-mediated Storage Access Framework folder grant.
- [ ] Persist the granted tree URI.
- [ ] Resolve the destination without hard-coded volume IDs.
- [ ] Write atomically, validate the result, and provide rollback.
- [ ] Report permission failures without crashing the target app.

## Phase 4 — validation and release

- [ ] Build the Morphe patch bundle.
- [ ] Test each app separately on exact recorded versions.
- [ ] Test app restart, app update, denied permission, and missing volume.
- [ ] Confirm the app reads the file and verify the resulting behavior.
- [ ] Publish only app/version combinations that pass.

## Current blockers

1. Android scoped storage prevents assuming write access to arbitrary paths under `/storage/<volume-id>`.
2. Edits and Instants need their exact installed package/version metadata recorded.
3. APK fingerprints and startup hooks must be derived from the actual APKs, not guessed.
