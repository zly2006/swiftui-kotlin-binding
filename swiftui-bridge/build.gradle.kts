plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}
val swiftOutput = layout.buildDirectory.dir("swift")
val nativeGenerated = rootProject.layout.buildDirectory.dir("generated/native-ui")
val compileSwift by tasks.registering(Exec::class) {
    dependsOn(":swiftui-codegen:generateBindings")
    inputs.files("src/swift/NativeTreeRuntime.swift", nativeGenerated.map { it.file("swift/GeneratedNativeUI.swift") })
    outputs.file(swiftOutput.map { it.file("libSwiftUIBinding.dylib") })
    doFirst { swiftOutput.get().asFile.mkdirs() }
    commandLine(
        "xcrun",
        "swiftc",
        "-emit-library",
        "-module-name",
        "SwiftUIBinding",
        "-swift-version",
        "5",
        "-default-isolation",
        "MainActor",
        "-target",
        "arm64-apple-macos15.0",
        "src/swift/NativeTreeRuntime.swift",
        nativeGenerated
            .get()
            .file("swift/GeneratedNativeUI.swift")
            .asFile.absolutePath,
        "-Xlinker",
        "-install_name",
        "-Xlinker",
        "@rpath/libSwiftUIBinding.dylib",
        "-o",
        swiftOutput
            .get()
            .file("libSwiftUIBinding.dylib")
            .asFile.absolutePath,
    )
}
val compileSwiftStatic by tasks.registering(Exec::class) {
    dependsOn(":swiftui-codegen:generateBindings")
    inputs.files("src/swift/NativeTreeRuntime.swift", nativeGenerated.map { it.file("swift/GeneratedNativeUI.swift") })
    outputs.file(swiftOutput.map { it.file("libSwiftUIBinding.a") })
    doFirst { swiftOutput.get().asFile.mkdirs() }
    commandLine(
        "xcrun",
        "swiftc",
        "-emit-library",
        "-static",
        "-module-name",
        "SwiftUIBinding",
        "-swift-version",
        "5",
        "-default-isolation",
        "MainActor",
        "-target",
        "arm64-apple-macos15.0",
        "src/swift/NativeTreeRuntime.swift",
        nativeGenerated
            .get()
            .file("swift/GeneratedNativeUI.swift")
            .asFile.absolutePath,
        "-o",
        swiftOutput
            .get()
            .file("libSwiftUIBinding.a")
            .asFile.absolutePath,
    )
}

kotlin {
    macosArm64 {
        compilations.getByName("main").cinterops.create("nativeui") {
            definitionFile.set(project.file("src/nativeInterop/cinterop/NativeUI.def"))
            includeDirs(nativeGenerated.get().dir("include").asFile)
            extraOpts("-libraryPath", swiftOutput.get().asFile.absolutePath, "-staticLibrary", "libSwiftUIBinding.a")
        }
        binaries.all {
            freeCompilerArgs += listOf("-Xoverride-konan-properties=ignoreXcodeVersionCheck=true", "-Xpartial-linkage-loglevel=error")
        }
    }
}
tasks.matching { it.name.startsWith("cinterop") }.configureEach {
    dependsOn(compileSwiftStatic)
    inputs.file(nativeGenerated.map { it.file("include/NativeUI.h") })
    inputs.file(swiftOutput.map { it.file("libSwiftUIBinding.a") })
}
