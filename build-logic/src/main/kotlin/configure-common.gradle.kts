import dev.engine_room.gradle.libraryOf

plugins {
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft(libraryOf("minecraft"))
}
