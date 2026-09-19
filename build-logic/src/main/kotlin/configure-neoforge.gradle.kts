import dev.engine_room.gradle.Constants
import dev.engine_room.gradle.getExt
import dev.engine_room.gradle.versionOf
import net.neoforged.moddevgradle.internal.utils.StringUtils

plugins {
    id("net.neoforged.moddev")
}

val modId = getExt("mod_id")
val modName = getExt("mod_name")

neoForge {
    version = versionOf("neoforge")
    validateAccessTransformers = true

    mods.maybeCreate(modId).apply {
        sourceSet(sourceSets.getByName("main"))
    }

    runs {
        create("client") {
            client()

            jvmArguments.addAll(Constants.JVM_ARGUMENTS)
            systemProperties.putAll(Constants.SYSTEM_PROPERTIES)
            programArguments.addAll(Constants.PROGRAM_ARGUMENTS)
        }

        // We're a client mod, but we need to make sure we correctly render when playing on a server.
        create("server") {
            server()

            programArguments.add("--nogui")
        }

        configureEach {
            ideName = "$modName NeoForge ${StringUtils.capitalize(name)} ($path)"
            ideFolderName = "NeoForge"

            systemProperty("forge.logging.markers", "")
            systemProperty("forge.logging.console.level", "debug")
        }
    }
}
