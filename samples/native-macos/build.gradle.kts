plugins { kotlin("multiplatform"); kotlin("plugin.compose"); id("org.jetbrains.compose") }
val swiftDir = project(":swiftui-bridge").layout.buildDirectory.dir("swift")
kotlin {
    macosArm64 {
        binaries.executable {
            baseName = "NativeDemo"
            entryPoint = "me.zly2006.swiftui.samples.nativeapp.main"
            freeCompilerArgs += listOf("-Xoverride-konan-properties=ignoreXcodeVersionCheck=true", "-Xpartial-linkage-loglevel=error")
            linkerOpts("-L${swiftDir.get().asFile}", "-lSwiftUIBinding", "-Wl,-rpath,${swiftDir.get().asFile}", "-Wl,-rpath,@executable_path/../Frameworks")
        }
    }
    sourceSets.commonMain.dependencies { implementation(project(":swiftui-compose")); implementation(project(":samples:demo-ui")) }
}
tasks.matching { it.name.startsWith("link") }.configureEach { dependsOn(":swiftui-bridge:compileSwift") }

val localHost = rootProject.file("local-fixtures/host")
if (localHost.isDirectory) {
    kotlin.sourceSets.configureEach {
        if (name == "macosMain") kotlin.setSrcDirs(listOf(localHost))
    }
}
