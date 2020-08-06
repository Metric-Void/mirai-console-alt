@file:Suppress("UnstableApiUsage")

import kotlin.math.pow
import java.time.Duration

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

apply(plugin = "com.github.johnrengelman.shadow")

buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:6.0.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.Kotlin.stdlib}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.stdlib}")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4") // don"t use any other.
    }
}

allprojects {
    group = "net.mamoe"

    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        apply(plugin = "com.github.johnrengelman.shadow")
        val kotlin =
            (this as ExtensionAware).extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
                ?: return@afterEvaluate

        tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
            classifier = ""
        }

        val shadowJvmJar by tasks.creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
            group = "mirai"

            classifier = "jvm"

            val compilation = kotlin.target.compilations["main"]

            dependsOn(compilation.compileKotlinTask)

            from(compilation.output)

            configurations = listOf(compilation.compileDependencyFiles as Configuration)

            this.exclude { file ->
                file.name.endsWith(".sf", ignoreCase = true)
                    .also { if (it) println("excluded ${file.name}") }
            }

            this.manifest {
                this.attributes(
                    "Manifest-Version" to 1,
                    "Implementation-Vendor" to "Mamoe Technologies",
                    "Implementation-Title" to this@afterEvaluate.name.toString(),
                    "Implementation-Version" to this@afterEvaluate.version.toString()
                )
            }
        }

        val githubUpload by tasks.creating {
            group = "mirai"
            dependsOn(tasks.getByName("shadowJar"))

            doFirst {
                timeout.set(Duration.ofHours(3) as Duration)
                findLatestFile()?.let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.GitHub.upload(
                            file,
                            "https://api.github.com/repos/mamoe/mirai-repo/contents/shadow/${project.name}/$filename",
                            project
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("GitHub Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }
        }

        val cuiCloudUpload by tasks.creating {
            group = "mirai"
            dependsOn(tasks.getByName("shadowJar"))

            doFirst {
                timeout.set(Duration.ofHours(3) as Duration)
                findLatestFile()?.let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.CuiCloud.upload(
                            file,
                            project
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("CuiCloud Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }

        }
    }
}


fun Project.findLatestFile(): Map.Entry<String, File> {
    return File(projectDir, "build/libs").walk()
        .filter { it.isFile }
        .onEach { println("all files=$it") }
        .filter { it.name.matches(Regex("""${project.name}-[0-9][0-9]*(\.[0-9]*)*.*\.jar""")) }
        .onEach { println("matched file: ${it.name}") }
        .associateBy { it.nameWithoutExtension.substringAfterLast('-') }
        .onEach { println("versions: $it") }
        .maxBy { (version, file) ->
            version.split('.').let {
                if (it.size == 2) it + "0"
                else it
            }.reversed().foldIndexed(0) { index: Int, acc: Int, s: String ->
                acc + 100.0.pow(index).toInt() * (s.toIntOrNull() ?: 0)
            }
        } ?: error("cannot find any file to upload")
}
