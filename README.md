# Meta Patches

[![Build](https://github.com/hemk651/meta-patches/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/hemk651/meta-patches/actions/workflows/build.yml)
[![Release](https://github.com/hemk651/meta-patches/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/hemk651/meta-patches/actions/workflows/release.yml)

An independent [Morphe](https://github.com/MorpheApp) patch source exploring shared MetaConfig overrides for Meta Android applications.

> **Branch:** `main` · **Release baseline:** `v1.0.0` · **Status:** compatibility declarations and repository foundation
>
> This project is an independent fork of [InstaFL](https://github.com/mamiiblt/instafel) by [mamiiblt](https://github.com/mamiiblt) (Muhammed Ali Bulut). The project is not affiliated with Meta, Instagram, or Morphe.

## Quick links

- [Add this source to Morphe](https://morphe.software/add-source?github=hemk651/meta-patches)
- [Latest main release](https://github.com/hemk651/meta-patches/releases)
- [Development branch](https://github.com/hemk651/meta-patches/tree/dev)
- [Feature and implementation status](docs/feature-status.md)
- [Supported app identifiers](docs/supported-apps.md)
- [Implementation plan](docs/implementation-plan.md)
- [Version compatibility policy](docs/version-policy.md)
- [Dynamic storage-path notes](docs/storage-path.md)

## Morphe patches

The list below is generated from `patches-list.json` during the release workflow. Do not edit the generated section by hand.

<!-- PATCHES_START -->
> **[v1.0.0](https://github.com/hemk651/meta-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 Edits&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.basel`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Edits — compatibility entry](#edits-compatibility-entry) | Registers Edits package compatibility. Runtime config injection is not included yet. |  |

</details>

<details open>
<summary>📦 Instagram&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.android`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Instagram — compatibility entry](#instagram-compatibility-entry) | Registers Instagram package compatibility. Runtime config injection is not included yet. |  |

</details>

<details open>
<summary>📦 Instants&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.moonshot`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Instants — compatibility entry](#instants-compatibility-entry) | Registers Instants package compatibility. Runtime config injection is not included yet. |  |

</details>

<details open>
<summary>📦 Threads&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.barcelona`

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Threads — compatibility entry](#threads-compatibility-entry) | Registers Threads package compatibility. Runtime config injection is not included yet. |  |

</details>

<!-- PATCHES_END -->

## What this branch currently provides

The `main` branch is the conservative v1.0.0 baseline. It contains four Morphe compatibility declarations, a shared MetaConfig JSON payload, Gradle project configuration, patch-list generation, validation, and GitHub Actions build/release workflows.

**Important:** the four entries register package compatibility only. They do not currently inject the configuration at runtime, unlock developer options, or modify the target apps' behavior. A patch appearing in Morphe is not evidence that its feature is implemented or device-tested.

| App | Android package | Main-branch state |
|---|---|---|
| Instagram | `com.instagram.android` | Compatibility declaration only |
| Threads | `com.instagram.barcelona` | Compatibility declaration only |
| Edits | `com.instagram.basel` | Compatibility declaration only |
| Instants | `com.instagram.moonshot` | Compatibility declaration only; package identity should be confirmed on-device |

For the functional Instagram and Instants implementation, see the [dev branch](https://github.com/hemk651/meta-patches/tree/dev). The two branches intentionally represent different project states.

## Configuration

The canonical payload is [`config/mc_overrides.json`](config/mc_overrides.json). The validator checks that the JSON is valid and matches the reviewed values:

```bash
python3 scripts/validate_config.py
```

Validation checks repository data only. It does not prove that an Android app loads the payload or that a feature is enabled.

## Build from source

Requirements:

- JDK 21
- Git
- Network access to resolve Gradle and Morphe dependencies

Build the Morphe patch bundle:

```bash
./gradlew :patches:buildAndroid --no-daemon
```

The GitHub Actions build workflow runs this task on pushes and pull requests targeting `main` or `dev`. The release workflow uses semantic-release to publish branch releases.

## Repository layout

| Path | Purpose |
|---|---|
| `patches/` | Morphe patch declarations and patch-list generation |
| `extensions/extension/` | Android extension module |
| `config/mc_overrides.json` | Canonical MetaConfig payload |
| `scripts/validate_config.py` | Payload validation |
| `.github/scripts/` | Generated README patch-list helper |
| `.github/workflows/` | Build and release automation |
| `docs/` | Compatibility, implementation, and storage documentation |

## Compatibility and safety

Morphe's version-agnostic target declarations are package selectors, not universal compatibility guarantees. App updates can change internal classes, methods, startup behavior, and configuration loading. See the [version policy](docs/version-policy.md) before treating any target as supported.

MetaConfig experiments can be unstable, account-specific, or carry service/account risk. Use at your own discretion and keep a recoverable copy of important app data. This project is independent and is not endorsed by Meta or Morphe.

## Credits and licensing

Meta Patches builds on ideas and code from [InstaFL](https://github.com/mamiiblt/instafel). See [NOTICE](NOTICE) for attribution and [CHANGELOG.md](CHANGELOG.md) for release history. The project is licensed under GPL-3.0; see the repository's license and notice files.
