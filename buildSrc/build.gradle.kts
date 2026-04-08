plugins {
    `kotlin-dsl`
    idea
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForged"
    }
    maven("https://maven.fabricmc.net/") {
        name = "FabricMC"
    }
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
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
    implementation(libs.fabric.loom)
    implementation(libs.mdg)
}
