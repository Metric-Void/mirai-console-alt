plugins {
    id("kotlinx-serialization")
    id("kotlin")
    id("java")
    maven
}


apply(plugin = "com.github.johnrengelman.shadow")

version = Versions.Mirai.console

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.MiraiConsoleTerminalLoader"
    }
}

kotlin {
    sourceSets {
        all {

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.useExperimentalAnnotation("kotlin.OptIn")
            languageSettings.progressiveMode = true
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
        }
    }
}

dependencies {
    compileOnly("net.mamoe:mirai-core-qqandroid:${Versions.Mirai.core}")
    api(project(":mirai-console"))
    api(group = "com.googlecode.lanterna", name = "lanterna", version = "3.0.2")
}
val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}
