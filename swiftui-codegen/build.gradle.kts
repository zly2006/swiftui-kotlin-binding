plugins {
    kotlin("jvm")
    application
}
dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
}
application { mainClass.set("me.zly2006.swiftui.generator.MainKt") }
val generatedDir = rootProject.layout.buildDirectory.dir("generated/native-ui")
tasks.register<JavaExec>("generateBindings") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(application.mainClass)
    args("generate", generatedDir.get().asFile.absolutePath)
    inputs.files(fileTree("src/main"))
    outputs.dir(generatedDir)
}
tasks.register<JavaExec>("showWhitelist") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(application.mainClass)
    args("whitelist")
}

tasks.register<JavaExec>("showCoverage") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(application.mainClass)
    args("coverage")
}
