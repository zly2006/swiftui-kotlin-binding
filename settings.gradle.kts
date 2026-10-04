pluginManagement { repositories { mavenCentral(); gradlePluginPortal(); google() } }
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement { repositories { mavenCentral(); google() } }
rootProject.name = "swiftui-kotlin-binding"
include(":swiftui-codegen", ":swiftui-bridge", ":swiftui-compose", ":samples:demo-ui", ":samples:native-macos")
include(":samples:sample-host", ":samples:capytimer", ":samples:liquid-glass")
