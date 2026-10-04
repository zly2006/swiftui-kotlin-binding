plugins { kotlin("multiplatform"); kotlin("plugin.compose"); id("org.jetbrains.compose") }
val generated = rootProject.layout.buildDirectory.dir("generated/native-ui")
kotlin {
    macosArm64 { binaries.all { freeCompilerArgs += listOf("-Xoverride-konan-properties=ignoreXcodeVersionCheck=true", "-Xpartial-linkage-loglevel=error") } }
    jvm()
    sourceSets {
        commonMain {
            kotlin.srcDir(generated.map { it.dir("common") })
            dependencies {
                api(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            }
        }
        macosMain {
            kotlin.srcDir(generated.map { it.dir("macos") })
            dependencies { implementation(project(":swiftui-bridge")) }
        }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmTest.dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0") }
    }
}
tasks.matching { it.name.startsWith("compileKotlin") }.configureEach { dependsOn(":swiftui-codegen:generateBindings") }
