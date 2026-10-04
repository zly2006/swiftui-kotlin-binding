plugins {
    kotlin("multiplatform") version "2.4.0" apply false
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.compose") version "2.4.0" apply false
    id("org.jetbrains.compose") version "1.11.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}
allprojects {
    group = "me.zly2006"
    version = "0.1.0-SNAPSHOT"
}
