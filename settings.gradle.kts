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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}

rootProject.name = "Flywheel"

include("common")
include("fabric")
include("neoforge")
include("vanillinNeoForge")
include("vanillinFabric")
