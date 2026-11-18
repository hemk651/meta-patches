# Feature and implementation status — main

**Branch:** `main` · **Release baseline:** `v1.0.0`

This page describes the code present on `main`, not the more advanced implementation on `dev`.

## Current capabilities

| Component | Present in repository | What it currently does |
|---|---:|---|
| Morphe patch declarations | Yes | Registers four package compatibility entries |
| Canonical config | Yes | Stores the reviewed `config/mc_overrides.json` payload |
| Config validator | Yes | Checks that the JSON matches the canonical expected values |
| Patch-list generation | Yes | Generates Morphe metadata and the README patch section |
| Gradle build workflow | Yes | Builds the Morphe patch bundle on CI |
| Semantic release workflow | Yes | Publishes branch releases |
| Runtime MobileConfig writer | No | Not implemented on this branch |
| Developer-options unlock | No | Not implemented on this branch |
| Mods menu / extension features | No | Not implemented on this branch |
| Functional Instants/gallery patches | No | Not implemented on this branch |

## Registered targets

- Instagram — `com.instagram.android`
- Threads — `com.instagram.barcelona`
- Edits — `com.instagram.basel`
- Instants — `com.instagram.moonshot`

A registered target is not the same as a tested functional patch. The four entries on this branch are explicitly compatibility-only.

## Configuration validation

Run:

```bash
python3 scripts/validate_config.py
```

The script validates the canonical payload against the expected JSON object. It does not install the file into an app, inspect an APK, or verify a runtime feature.

## Build

```bash
./gradlew :patches:buildAndroid --no-daemon
```

The CI workflow uses JDK 21 and runs this task for pushes and pull requests targeting `main` and `dev`.

## Relationship to dev

The `dev` branch contains a later implementation line for Instagram and Instagram Instants, including runtime MobileConfig installation, developer-options unlocking, the Mods menu, and additional patches. Keep the branch descriptions separate: do not copy dev-only features into this page or imply that they ship in the main v1.0.0 baseline.
