plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}
evaluationDependsOn(":swiftui-codegen")
val generated =
    project(":swiftui-codegen").tasks.named("generateBindings").map {
        rootProject.layout.buildDirectory
            .dir("generated/native-ui")
            .get()
    }
kotlin {
    macosArm64 {
        binaries.all {
            freeCompilerArgs +=
                listOf("-Xoverride-konan-properties=ignoreXcodeVersionCheck=true", "-Xpartial-linkage-loglevel=error")
        }
    }
    jvm()
    sourceSets {
        commonMain {
            kotlin.srcDir(generated.map { it.dir("common") })
            dependencies {
                api(compose.runtime)
                api(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            }
        }
        macosMain {
            kotlin.srcDir(generated.map { it.dir("macos") })
            dependencies { implementation(project(":swiftui-bridge")) }
        }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmTest.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
            implementation(compose.foundation)
        }
    }
}

// Dependency changes must invalidate the metadata consumed by common source sets and IDE import.
tasks.named("generateProjectStructureMetadata").configure {
    inputs.file(layout.projectDirectory.file("build.gradle.kts"))
}
