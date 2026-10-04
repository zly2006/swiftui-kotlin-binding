import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("multiplatform") version "2.4.0" apply false
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.compose") version "2.4.0" apply false
    id("org.jetbrains.compose") version "1.11.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}
allprojects {
    group = "me.zly2006.swiftui"
    version = "0.1.0"

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        filter {
            val generatedBindings = rootProject.layout.buildDirectory.dir("generated/native-ui")
            exclude { it.file.toPath().startsWith(generatedBindings.get().asFile.toPath()) }
        }
    }
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        check(project.name in setOf("swiftui-compose", "swiftui-bridge")) { "Only runtime library modules may be published" }
        tasks.matching { it.name.startsWith("publish") && it.name.contains("MavenCentral") }.configureEach {
            doFirst {
                check(providers.gradleProperty("allowCentralUpload").orNull == "true") {
                    "Central publishing is stopped. Explicitly authorize publishing before using -PallowCentralUpload=true."
                }
            }
        }
        extensions.configure<MavenPublishBaseExtension> {
            coordinates(project.group.toString(), project.name, project.version.toString())
            publishToMavenCentral(automaticRelease = false, validateDeployment = DeploymentValidation.VALIDATED)
            signAllPublications()
            pom {
                name.set("SwiftUI Kotlin Binding — ${project.name}")
                description.set(
                    when (project.name) {
                        "swiftui-compose" ->
                            "Kotlin Composables backed by native SwiftUI, with Compose Runtime state and incremental updates."
                        "swiftui-bridge" -> "Typed Kotlin/Native interoperability and an embedded SwiftUI native bridge for macOS arm64."
                        else -> error("Unexpected publication module")
                    },
                )
                url.set("https://github.com/zly2006/swiftui-kotlin-binding")
                inceptionYear.set("2026")
                licenses {
                    license {
                        name.set("GNU General Public License, version 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("zly2006")
                        name.set("zly2006")
                        url.set("https://github.com/zly2006")
                    }
                }
                scm {
                    url.set("https://github.com/zly2006/swiftui-kotlin-binding")
                    connection.set("scm:git:https://github.com/zly2006/swiftui-kotlin-binding.git")
                    developerConnection.set("scm:git:ssh://git@github.com/zly2006/swiftui-kotlin-binding.git")
                    tag.set("swiftui-v${project.version}")
                }
            }
        }
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "ReleaseTest"
                    url = uri(rootProject.layout.buildDirectory.dir("release-repo"))
                }
            }
        }
        tasks.withType<Jar>().configureEach {
            from(rootProject.file("LICENSE")) { into("META-INF") }
        }
    }
}
