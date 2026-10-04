plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}
kotlin {
    macosArm64()
    sourceSets.commonMain.dependencies { api(project(":swiftui-compose")) }
}
