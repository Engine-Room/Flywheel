pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }
        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven("https://repo.spongepowered.org/repository/maven-public")
        maven("https://maven.parchmentmc.org")
    }

    plugins {
        val arch_loom_version: String by settings
        id("dev.architectury.loom") version arch_loom_version
    }
}

rootProject.name = "Flywheel"

include("common")
include("fabric")
include("forge")
