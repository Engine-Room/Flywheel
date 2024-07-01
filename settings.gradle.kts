pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.minecraftforge.net/") {
            name = "MinecraftForge"
        }
        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven("https://repo.spongepowered.org/repository/maven-public")
        maven("https://maven.parchmentmc.org")
    }

    // FIXME: This doesn't do anything. The actual version is always the one defined in buildSrc/build.gradle.kts.
//    plugins {
//        val arch_loom_version: String by settings
//        id("dev.architectury.loom") version arch_loom_version
//    }
}

rootProject.name = "Flywheel"

include("common")
include("fabric")
include("forge")
