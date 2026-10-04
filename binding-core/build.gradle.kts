plugins { kotlin("multiplatform") }
val swiftOutput = layout.buildDirectory.dir("swift")
val nativeGenerated = rootProject.layout.buildDirectory.dir("generated/native-ui")
val compileSwift by tasks.registering(Exec::class) {
    dependsOn(":binding-generator:generateBindings")
    inputs.files("src/swift/NativeTreeRuntime.swift", nativeGenerated.map { it.file("swift/GeneratedNativeUI.swift") })
    outputs.file(swiftOutput.map { it.file("libSwiftUIBinding.dylib") })
    doFirst { swiftOutput.get().asFile.mkdirs() }
    commandLine("xcrun", "swiftc", "-emit-library", "-module-name", "SwiftUIBinding", "-swift-version", "5", "-default-isolation", "MainActor", "-target", "arm64-apple-macos15.0", "src/swift/NativeTreeRuntime.swift", nativeGenerated.get().file("swift/GeneratedNativeUI.swift").asFile.absolutePath, "-Xlinker", "-install_name", "-Xlinker", "@rpath/libSwiftUIBinding.dylib", "-o", swiftOutput.get().file("libSwiftUIBinding.dylib").asFile.absolutePath)
}
kotlin {
    macosArm64 {
        compilations.getByName("main").cinterops.create("nativeui") {
            definitionFile.set(project.file("src/nativeInterop/cinterop/NativeUI.def"))
            includeDirs(nativeGenerated.get().dir("include").asFile)
        }
        binaries.all {
            freeCompilerArgs += listOf("-Xoverride-konan-properties=ignoreXcodeVersionCheck=true", "-Xpartial-linkage-loglevel=error")
            linkerOpts("-L${swiftOutput.get().asFile}", "-lSwiftUIBinding", "-Wl,-rpath,${swiftOutput.get().asFile}")
        }
    }
}
tasks.matching { it.name.startsWith("cinterop") }.configureEach {
    dependsOn(":binding-generator:generateBindings")
    inputs.file(nativeGenerated.map { it.file("include/NativeUI.h") })
}
tasks.matching { it.name.startsWith("link") }.configureEach { dependsOn(compileSwift) }
