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
}

rootProject.name = "Flywheel"

include("common")
include("fabric")
include("forge")
