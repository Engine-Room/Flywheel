plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
}

gradlePlugin {
    plugins {
        create("flywheelGradlePlugin") {
            id = libs.plugins.flywheel.gradle.get().toString()
            implementationClass = "dev.engine_room.gradle.FlywheelGradlePlugin"
        }
    }
}

dependencies {
    implementation(libs.fabric.loom)
    implementation(libs.mdg)
}
