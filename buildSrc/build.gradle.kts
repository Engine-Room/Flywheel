plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.9.23"
    `kotlin-dsl`
}

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

gradlePlugin {
    plugins {
        create("platformPlugin") {
            id = "flywheel.platform"
            implementationClass = "dev.engine_room.gradle.platform.PlatformPlugin"
        }
        create("subprojectPlugin") {
            id = "flywheel.subproject"
            implementationClass = "dev.engine_room.gradle.subproject.SubprojectPlugin"
        }
    }
}

dependencies {
    // FIXME: This should not hard-code the Loom version.
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.7.+")
}
