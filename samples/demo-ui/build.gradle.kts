plugins { kotlin("multiplatform"); kotlin("plugin.compose"); id("org.jetbrains.compose") }
kotlin {
    macosArm64()
    jvm()
    sourceSets.commonMain.dependencies { implementation(project(":native-compose")) }
}

val localUi = rootProject.file("local-fixtures/ui")
if (localUi.isDirectory) {
    kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(localUi)
}
