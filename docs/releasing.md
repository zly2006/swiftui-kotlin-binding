# Releasing

Publish only `swiftui-compose` and `swiftui-bridge`, using Maven group `me.zly2006.swiftui`. `swiftui-codegen` is a repository build tool and is not part of the Maven distribution. Sample pages, screenshots, local reference sources, and test hosts are not dependencies of the published libraries.

## Native distribution

`swiftui-bridge:compileSwiftStatic` compiles the official SwiftUI adapters into `libSwiftUIBinding.a`. The cinterop artifact embeds that archive in its `.klib`, so consuming applications resolve it through Maven metadata. Its linker options reference system Swift libraries and Apple frameworks, never the publisher's checkout. The optional dynamic bridge build remains available for existing local app packaging.

The static archive uses the same Swift compiler settings as the verified dynamic bridge. Optimized compilation is not enabled in 0.1.0: the current Swift compiler's optimization pass crashes on a generic relay deinitializer. This choice must be revisited with a tested compiler update, rather than bypassing the failed compiler result.

## Local release gate

Set the version in the root build, then stage signed publications in a local Maven repository:

```sh
./gradlew :swiftui-codegen:test :swiftui-compose:jvmTest
./gradlew :swiftui-bridge:publishAllPublicationsToReleaseTestRepository \
  :swiftui-compose:publishAllPublicationsToReleaseTestRepository
./gradlew -p "$RELEASE_CONSUMER_DIR" linkDebugExecutableMacosArm64 \
  -PbindingRepository="file://$PWD/build/release-repo"
```

Create an independent consumer from [Getting started](getting-started.md) outside the tracked source tree and set `RELEASE_CONSUMER_DIR` to its directory. Consumer validation code stays local and is excluded from Git tracking. Run it in initial and updated snapshot modes. Verify the actual native window, state changes without node recreation, static scheduling, and resource release. Inspect the staged POMs, signatures, source archives, native archive, and transitive metadata before uploading. Published source archives must include generated public Kotlin declarations as well as maintained sources.

## Maven Central

The build uses the [Vanniktech Maven Publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/central/) with automatic release disabled. Central upload tasks are blocked unless `-PallowCentralUpload=true` is explicitly provided after a user request to publish. Configure Central user tokens and the signing key in the user's Gradle properties or environment; never store credentials or private keys in this repository.

```sh
./gradlew :swiftui-bridge:publishToMavenCentral \
  :swiftui-compose:publishToMavenCentral -PallowCentralUpload=true
```

Use an empty Central staging directory; archive outputs from previous groups before staging a new release. After upload, verify the staged group, module list, version, signatures, and consumer behavior before explicitly releasing the validated deployment. Commit the exact release source and tag it as `swiftui-v<version>`. The older `v0.1.0` tag remains the source reference for the accidental parent-group publication. Upload only after the local gate passes. Maven Central versions are immutable: a correction requires a new version. The final gate is to download the public artifacts and build the standalone consumer without `bindingRepository`, not merely to finish an upload task.

The library POM declares GPL-3.0-only. Upstream sample MIT notices remain in their own directories and do not change the library's declared license.

## Coordinate correction

The Maven group identifies this library and is `me.zly2006.swiftui`; permission to a parent namespace does not change these coordinates. Public Kotlin packages remain under `me.zly2006.swiftui`.

The earlier `me.zly2006` deployment for version 0.1.0 reached `PUBLISHED` before the stop request. Those immutable artifacts are superseded and must not be used for the corrected integration. No artifacts have been uploaded under `me.zly2006.swiftui` in this correction. Publishing remains paused until explicitly requested.
