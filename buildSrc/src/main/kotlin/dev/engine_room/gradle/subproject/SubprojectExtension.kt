package dev.engine_room.gradle.subproject

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import java.io.File
import java.net.URI
import java.util.*

open class SubprojectExtension(val project: Project) {
    fun init(archiveBase: String, group: String, version: String) {
        loadSecrets()

        setBaseProperties(archiveBase, group, version)
        setupJava()
        addRepositories()
        setupLoom()
        setupDependencies()
        configureTasks()
        setupPublishing()
    }

    val buildNumber: String? by lazy {
        val dev = System.getenv("RELEASE")?.contentEquals("false", true) ?: true

        if (dev) {
            System.getenv("BUILD_NUMBER")
        } else {
            null
        }
    }

    private fun setBaseProperties(archiveBase: String, group: String, version: String) {
        val versionSuffix = if (buildNumber != null) "-${buildNumber}" else ""

        project.group = project.property(group) as String
        project.version = "${project.property(version)}${versionSuffix}"

        project.the<BasePluginExtension>().apply {
            archivesName = "${archiveBase}-${project.property("artifact_minecraft_version")}"
        }
    }

    private fun setupLoom() {
        val loom = project.the<LoomGradleExtensionAPI>()
        loom.silentMojangMappingsLicense()
    }

    private fun setupJava() {
        val java_version: String by project

        project.the<JavaPluginExtension>().apply {
            val javaVersion = JavaVersion.toVersion(java_version)
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion

            toolchain.languageVersion = JavaLanguageVersion.of(java_version)

            withSourcesJar()
            withJavadocJar()
        }
    }

    private fun addRepositories() {
        project.repositories.apply {
            mavenCentral()
            maven("https://maven.parchmentmc.org") {
                name = "ParchmentMC"
            }
            maven("https://maven.tterrag.com/") {
                name = "tterrag maven"
            }
            maven("https://www.cursemaven.com") {
                name = "CurseMaven"
                content {
                    includeGroup("curse.maven")
                }
            }
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
                content {
                    includeGroup("maven.modrinth")
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupDependencies() {
        project.dependencies.apply {
            val minecraft_version: String by project
            val parchment_minecraft_version: String by project
            val parchment_version: String by project
            val loom = project.the<LoomGradleExtensionAPI>()

            add("minecraft", "com.mojang:minecraft:${minecraft_version}")

            add("mappings", loom.layered {
                officialMojangMappings()
                parchment("org.parchmentmc.data:parchment-${parchment_minecraft_version}:${parchment_version}@zip")
            })

            add("api", "com.google.code.findbugs:jsr305:3.0.2")
        }
    }

    private fun configureTasks() {
        val java_version: String by project

        project.tasks.apply {
            // make builds reproducible
            withType<AbstractArchiveTask>().configureEach {
                isPreserveFileTimestamps = false
                isReproducibleFileOrder = true
            }

            // module metadata is often broken on multi-platform projects
            withType<GenerateModuleMetadata>().configureEach {
                enabled = false
            }

            withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
                options.release = Integer.parseInt(java_version)
                options.compilerArgs.add("-Xdiags:verbose")
            }

            withType<Jar>().configureEach {
                from("${project.rootDir}/LICENSE.md") {
                    into("META-INF")
                }
            }

            withType<Javadoc>().configureEach {
                options.optionFiles(project.rootProject.file("javadoc-options.txt"))
                options.encoding = "UTF-8"
            }
        }
    }

    private fun setupPublishing() {
        project.the<PublishingExtension>().repositories.apply {
            maven("file://${project.rootProject.projectDir}/mcmodsrepo")

            if (project.hasProperty("mavendir")) {
                maven(project.rootProject.file(project.property("mavendir") as String))
            }

            // Sets maven credentials if they are provided. This is generally
            // only used for external/remote uploads.
            if (project.hasProperty("mavenUsername") && project.hasProperty("mavenPassword") && project.hasProperty("mavenURL")) {
                project.logger.lifecycle("Adding maven from secrets")

                maven {
                    credentials {
                        username = project.property("mavenUsername") as String
                        password = project.property("mavenPassword") as String
                    }
                    url = URI.create(project.property("mavenURL") as String)
                }
            }
        }
    }

    private fun loadSecrets() {
        // Detects the secrets file provided by CI and loads it into the project properties
        if (project.rootProject.hasProperty("secretFile")) {
            val secretsFile = project.rootProject.file(project.rootProject.property("secretFile") as String)

            if (secretsFile.exists() && secretsFile.name.endsWith(".properties")) {
                project.logger.lifecycle("Loading secrets")

                loadProperties(secretsFile)
            }
        }
    }

    private fun loadProperties(propertyFile: File) {
        if (propertyFile.exists()) {
            val properties = Properties().apply { load(propertyFile.inputStream()) }

            var count = 0
            for (entry in properties.entries) {
                if (entry.key is String) {
                    project.extra[entry.key as String] = entry.value as String
                    count++
                }
            }

            project.logger.lifecycle("Loaded $count properties")
        } else {
            project.logger.warn("The property file ${propertyFile.getName()} does not exist")
        }
    }
}
