# 🧩 Meta Patches

A [Morphe](https://morphe.software) patch bundle that unlocks Meta's internal **Employee Options /
developer options** in Instagram, Edits and Instants.

## ❓ About

The bundle ships one package-agnostic patch, **Unlock employee options**. It injects a tiny startup
hook into the app's `Application` class that writes a `mc_overrides.json` file into the app's
storage before Meta's MobileConfig framework initializes. Meta's apps then enable their hidden
internal options menu, including developer options.

The same patch works for all supported apps:

| App | Package name |
|-----|--------------|
| 📷 Instagram | `com.instagram.android` |
| 🎬 Edits | `com.instagram.basel` |
| ⚡ Instants | `com.instagram.moonshot` |

### How to use these patches

Click the button below, or manually add this repo url to Morphe Manager: https://github.com/hemk651/meta-patches

<a href="https://morphe.software/add-source?github=hemk651/meta-patches">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

### The overrides file

The patch writes the following JSON by default (customizable through the
`mc_overrides_json` option):

```json
{"56474:":["0: : true"],"28538:":["0: : true"],"32432:":["1: : true"],"_qe_overrides_":[]}
```

To keep every write failure from breaking the app, the file is written atomically (temp file +
rename), idempotently (an identical file is left untouched) and every failure is swallowed with
a log line. The file is written to every location Meta apps are known to read it from:

- `<app filesDir>/mobileconfig/mc_overrides.json` — the canonical internal location, always writable.
- `<storage>/Android/data/<package>/files/mobileconfig/mc_overrides.json` — for every mounted
  storage volume.
- `<storage root>/data/files/mobileconfig/mc_overrides.json` — the layout some devices read
  directly from the volume root. Writes there fail silently on modern scoped storage, which is
  harmless.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.
     
     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END 
     comment blocks entirely. -->

#### A list of patches will automatically be shown here after the first patches release is created.

&nbsp;

<!-- PATCHES_END -->

## 🧑‍💻 Dev usage

To develop and release these patches:

- **Make all changes to the `alpha` branch.** `main` holds the stable releases.
- For local development work build your patches using the gradle task `./gradlew buildAndroid` to
  generate the mpp file found in `patches/build/libs/patches-*.mpp`. Apply your patches locally
  using Morphe Desktop tool like any other patch bundle.
- Always use [Semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index) messages for commits. To keep it simple use only 3 commit message types: 
  - `feat: Added a new feature`
  - `fix: Some problem now fixed`
  - `chore: Random change you do not want in the user facing changelog`
- Commits of `fix:` and `feat:` will automatically generate new pre-releases and `chore:` will not create a new release.
- Users can apply the alpha branch releases by enabling `pre-release` in Morphe Manager patch sources.
- When the alpha branch is ready, and you want a stable release, merge alpha branch to main (do not squash, and only merge).
- **Always use semantic release (release.yml)**. Do not manually upload or create releases by hand
  because many files must be updated and release.yml handles everything.
- Do not manually edit or manually commit any generated files such as: `patches-list.json`,
  `patches-bundle.json`, `CHANGELOG.md`. These files will be automatically updated by `release.yml`.
- Do not force push any semantic release commits as that will break all future releases.

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

Meta Patches are licensed under the [GNU General Public License v3.0](LICENSE).
