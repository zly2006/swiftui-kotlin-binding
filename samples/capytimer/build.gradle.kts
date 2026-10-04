plugins { kotlin("multiplatform"); kotlin("plugin.compose"); id("org.jetbrains.compose") }
val swiftDir = project(":swiftui-bridge").layout.buildDirectory.dir("swift")
kotlin {
    macosArm64 {
        binaries.executable {
            baseName = "CapyTimerMock"
            entryPoint = "me.zly2006.swiftui.samples.capytimer.main"
            freeCompilerArgs += listOf("-Xoverride-konan-properties=ignoreXcodeVersionCheck=true", "-Xpartial-linkage-loglevel=error")
            linkerOpts("-L${swiftDir.get().asFile}", "-lSwiftUIBinding", "-Wl,-rpath,${swiftDir.get().asFile}", "-Wl,-rpath,@executable_path/../Frameworks")
        }
    }
    jvm()
    sourceSets.commonMain.dependencies { implementation(project(":swiftui-compose")) }
    sourceSets.getByName("macosArm64Main").dependencies { implementation(project(":samples:sample-host")) }
}
tasks.matching { it.name.startsWith("link") }.configureEach { dependsOn(":swiftui-bridge:compileSwift") }
