# Release Process

This document describes the release process for the Selenium Foundation ecosystem,
which consists of three interdependent projects:

- **[selenium-bom](https://github.com/sbabcoc/selenium-bom)** — Bill of Materials
- **[Selenium-Foundation](https://github.com/sbabcoc/Selenium-Foundation)** — Core automation framework
- **[selenium-grid-manager](https://github.com/sbabcoc/selenium-grid-manager)** — Local Grid management

## Prerequisites

- GPG key configured for signing
- Sonatype Central Portal credentials configured in `~/.m2/settings.xml` under server ID `ossrh`
- All three projects cloned locally and up to date
- Gradle wrapper present in each project

## Version Numbering

All three projects use the same base version number (e.g. `35.0.0`), with
variant suffixes applied by the build:

| Project | Artifact | Version format |
|---------|----------|----------------|
| `selenium-bom` | `selenium-bom-s3` | `35.0.0` |
| `selenium-bom` | `selenium-bom-s4` | `35.0.0` |
| `Selenium-Foundation` | `selenium-foundation` (s3) | `35.0.0-s3` |
| `Selenium-Foundation` | `selenium-foundation` (s4) | `35.0.0-s4` |
| `selenium-grid-manager` | `selenium-grid-manager` (s3) | `35.0.0-s3` |
| `selenium-grid-manager` | `selenium-grid-manager` (s4) | `35.0.0-s4` |

## Dependency Order

The projects must be released in this order due to dependencies:

```
selenium-bom → selenium-foundation → selenium-grid-manager
```

## Pre-Release Checklist

Before tagging, verify:

1. All changes are committed and pushed in all three projects
2. All three projects build cleanly:
   ```bash
   cd selenium-bom && ./gradlew build
   cd ../Selenium-Foundation && ./gradlew build -Pprofile=selenium4
   cd ../selenium-grid-manager && ./gradlew build -Pprofile=selenium4
   ```
3. `selenium-foundation` unit tests pass with current SNAPSHOT artifacts
4. README files are up to date

> **IMPORTANT**: Do a full build verification BEFORE tagging. If a bug is found
> after tagging, the tag must be deleted locally and remotely, the bug fixed,
> and the tag recreated on the new commit. Moving tags after publishing to
> Maven Central is not possible.

## Release Steps

### 1. Tag all three projects

All three projects must be tagged at the same base version. Version is derived
from Git tags by axion-release — no manual version editing is needed.

```bash
cd selenium-bom
git tag v35.0.0
git push origin v35.0.0

cd ../Selenium-Foundation
git tag v35.0.0
git push origin v35.0.0

cd ../selenium-grid-manager
git tag v35.0.0
git push origin v35.0.0
```

Verify all three produce the correct release version:

```bash
cd selenium-bom && ./gradlew properties | grep "^version:"
cd ../Selenium-Foundation && ./gradlew properties -Pprofile=selenium4 | grep "^version:"
cd ../selenium-grid-manager && ./gradlew properties -Pprofile=selenium4 | grep "^version:"
```

All three should show release versions (no `-SNAPSHOT` suffix).

### 2. Publish and install selenium-bom

`publish` and `closeAndReleaseStagingRepositories` MUST run in the same Gradle
invocation — the staging repository ID is not persisted between invocations.

```bash
cd selenium-bom
./gradlew publish closeAndReleaseStagingRepositories
./gradlew install
```

Verify both BOMs are in local Maven repository:
```bash
ls ~/.m2/repository/com/nordstrom/ui-tools/selenium-bom-s3/
ls ~/.m2/repository/com/nordstrom/ui-tools/selenium-bom-s4/
```

### 3. Publish and install selenium-foundation

Tests are skipped during publication to avoid the circular dependency on
`selenium-grid-manager`, which hasn't been published yet.

`install` is run alongside `publish` so `selenium-grid-manager` can resolve
`selenium-foundation` during its own build.

```bash
cd ../Selenium-Foundation
./gradlew install publish closeAndReleaseStagingRepositories -Pprofile=selenium4 -x test -x testNG
./gradlew install publish closeAndReleaseStagingRepositories -Pprofile=selenium3 -x test -x testNG
```

Verify artifacts are in local Maven repository:
```bash
ls ~/.m2/repository/com/nordstrom/ui-tools/selenium-foundation/
```

### 4. Publish and install selenium-grid-manager

```bash
cd ../selenium-grid-manager
./gradlew publish closeAndReleaseStagingRepositories -Pprofile=selenium4
./gradlew publish closeAndReleaseStagingRepositories -Pprofile=selenium3
./gradlew install -Pprofile=selenium4
./gradlew install -Pprofile=selenium3
```

### 5. Verify selenium-foundation tests

Now that `selenium-grid-manager` is published, run the full `selenium-foundation`
test suite to verify the release:

```bash
cd ../Selenium-Foundation
./gradlew test testNG -Pprofile=selenium4
```

### 6. Verify Maven Central publication

Check that all artifacts appear at:
**https://central.sonatype.com/publishing/deployments**

Propagation to Maven Central typically takes 10-30 minutes after the staging
repository is released.

## Moving a Tag After a Bug Fix

If a bug is found after tagging but before publishing to Maven Central:

```bash
# Fix the bug and commit
git add <files>
git commit -m "Fix <description>"

# Delete tag locally and remotely
git tag -d v35.0.0
git push origin :refs/tags/v35.0.0

# Recreate tag on new HEAD
git tag v35.0.0
git push origin v35.0.0
```

If the bug is found after publishing to Maven Central, the release cannot be
modified. A new patch release (e.g. `35.0.1`) must be made.

## Local Development Builds

For local development, use `install` instead of `publish`:

```bash
cd selenium-bom && ./gradlew install
cd ../Selenium-Foundation && ./gradlew install -Pprofile=selenium4
cd ../selenium-grid-manager && ./gradlew install -Pprofile=selenium4
```

During development, all three projects produce SNAPSHOT versions
(e.g. `35.0.1-SNAPSHOT`, `35.0.1-s4-SNAPSHOT`) when working on an
untagged commit.

## Dropping Failed Staging Repositories

If a publish fails after the staging repository is created, drop it via:

```bash
curl -X POST \
  -u "OSSRH_USERNAME:OSSRH_PASSWORD" \
  "https://ossrh-staging-api.central.sonatype.com/service/local/staging/bulk/drop" \
  -H "Content-Type: application/json" \
  -d '{"data":{"stagedRepositoryIds":["REPO_ID_1","REPO_ID_2"]}}'
```

The staging repository IDs are printed in the build output, e.g.:
```
Created staging repository 'com.nordstrom--abc123...' at https://...
```

A 204 response (no body) indicates success.

## Troubleshooting

**"Dependency version information is missing"** — The published POM has
versionless dependencies. Ensure `versionMapping` is configured in the
`mavenJava` publication block:
```groovy
versionMapping {
    usage('java-api') {
        fromResolutionOf('runtimeClasspath')
    }
    usage('java-runtime') {
        fromResolutionResult()
    }
}
```

**"No staging repository with name ossrh created"** — `publish` and
`closeAndReleaseStagingRepositories` were run in separate Gradle invocations.
Always run them together in a single command.

**BOM not found during build** — Install the BOM locally with `./gradlew install`
before building dependent projects.

**Version mismatch between projects** — Ensure all three projects are tagged
at the same base version. Check with:
```bash
./gradlew properties | grep "^version:"
```

**Version shows branch name (e.g. `35.0.0-pr-my-branch`)** — The
`branchVersionCreator` in `build.gradle` is missing or defined after
`scmVersion.version` is read. Ensure the `scmVersion` block (including
`branchVersionCreator`) appears BEFORE the version logic:
```groovy
def verBits = scmVersion.version.split('-')
```
