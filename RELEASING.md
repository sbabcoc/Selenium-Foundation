# Release Process

This document describes the release process for the Selenium Foundation ecosystem,
which consists of three interdependent projects:

- **[selenium-bom](https://github.com/sbabcoc/selenium-bom)** — Bill of Materials
- **[Selenium-Foundation](https://github.com/sbabcoc/Selenium-Foundation)** — Core automation framework
- **[selenium-grid-manager](https://github.com/sbabcoc/selenium-grid-manager)** — Local Grid management

## Prerequisites

- GPG key configured for signing
- Sonatype OSSRH credentials configured in `~/.m2/settings.xml`
- All three projects cloned locally and up to date
- Gradle wrapper present in each project

## Version Numbering

All three projects use the same base version number (e.g. `34.2.0`), with
variant suffixes applied by the build:

| Project | Artifact | Version format |
|---------|----------|---------------|
| `selenium-bom` | `selenium-bom-s3` | `34.2.0` |
| `selenium-bom` | `selenium-bom-s4` | `34.2.0` |
| `Selenium-Foundation` | `selenium-foundation` (s3) | `34.2.0-s3` |
| `Selenium-Foundation` | `selenium-foundation` (s4) | `34.2.0-s4` |
| `selenium-grid-manager` | `selenium-grid-manager` (s3) | `34.2.0-s3` |
| `selenium-grid-manager` | `selenium-grid-manager` (s4) | `34.2.0-s4` |

## Dependency Order

The projects must be released in this order due to dependencies:

```
selenium-bom → selenium-foundation → selenium-grid-manager
```

## Release Steps

### 1. Prepare

Ensure all changes are committed and pushed in all three projects.
Update `CHANGELOG.md` or release notes as appropriate.

### 2. Tag all three projects

All three projects must be tagged at the same base version:

```bash
cd selenium-bom
git tag v34.2.0
git push origin v34.2.0

cd ../Selenium-Foundation
git tag v34.2.0
git push origin v34.2.0

cd ../selenium-grid-manager
git tag v34.2.0
git push origin v34.2.0
```

### 3. Publish selenium-bom

```bash
cd selenium-bom
./gradlew publish
```

Verify both BOMs are published:
- `com.nordstrom.ui-tools:selenium-bom-s3:34.2.0`
- `com.nordstrom.ui-tools:selenium-bom-s4:34.2.0`

### 4. Publish selenium-foundation

Tests are skipped during publication to avoid the circular dependency on
`selenium-grid-manager`, which hasn't been published yet.

```bash
cd ../Selenium-Foundation
./gradlew publish -Pprofile=selenium4 -x test -x testNG
./gradlew publish -Pprofile=selenium3 -x test -x testNG
```

Verify both artifacts are published:
- `com.nordstrom.ui-tools:selenium-foundation:34.2.0-s4`
- `com.nordstrom.ui-tools:selenium-foundation:34.2.0-s3`

### 5. Publish selenium-grid-manager

Tests run as part of this build since `selenium-foundation` is now available.

```bash
cd ../selenium-grid-manager
./gradlew publish -Pprofile=selenium4
./gradlew publish -Pprofile=selenium3
```

Verify both artifacts are published:
- `com.nordstrom.ui-tools:selenium-grid-manager:34.2.0-s4`
- `com.nordstrom.ui-tools:selenium-grid-manager:34.2.0-s3`

### 6. Verify selenium-foundation tests

Now that `selenium-grid-manager` is published, run the full `selenium-foundation`
test suite to verify the release:

```bash
cd ../Selenium-Foundation
./gradlew test testNG -Pprofile=selenium4
```

### 7. Verify README versions

Each project's `README.md` is automatically updated with the release version
via axion-release hooks during the `publish` task. Verify the README files
reflect the new version and commit any changes:

```bash
cd selenium-bom && git add README.md && git commit -m "Update README for release v34.2.0" && git push
cd ../Selenium-Foundation && git add README.md && git commit -m "Update README for release v34.2.0" && git push
cd ../selenium-grid-manager && git add README.md && git commit -m "Update README for release v34.2.0" && git push
```

## Local Development Builds

For local development, use `install` instead of `publish` to install artifacts
to your local Maven repository (`~/.m2`):

```bash
# Install all three projects locally
cd selenium-bom && ./gradlew install
cd ../Selenium-Foundation && ./gradlew install -Pprofile=selenium4
cd ../selenium-grid-manager && ./gradlew install -Pprofile=selenium4
```

During development, all three projects will produce SNAPSHOT versions
(e.g. `34.2.0-SNAPSHOT`, `34.2.0-s4-SNAPSHOT`) when working on an
untagged commit.

## Troubleshooting

**Version mismatch between projects** — ensure all three projects are tagged
at the same base version. Check with:
```bash
./gradlew properties | grep "^version:"
```

**BOM not found** — ensure `selenium-bom` is installed/published before
building the other two projects.

**`selenium-foundation` tests fail after release** — check that
`selenium-grid-manager` was successfully published and is accessible
in your local Maven repository or Maven Central.
