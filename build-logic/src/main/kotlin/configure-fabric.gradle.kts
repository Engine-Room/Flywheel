import dev.engine_room.gradle.Constants
import dev.engine_room.gradle.getExt
import dev.engine_room.gradle.libraryOf
import net.fabricmc.loom.task.RenderDocRunTask
import net.fabricmc.loom.task.RenderDocRunUITask
import net.fabricmc.loom.task.RunGameTask
import net.fabricmc.loom.util.Strings

plugins {
    id("net.fabricmc.fabric-loom")
}

val modId = getExt("mod_id")
val modName = getExt("mod_name")


val tracyCapturePath = providers.gradleProperty("loom.tracyCapturePath").map { File(it) }
val renderDocCliPath = providers.gradleProperty("loom.renderDocCliPath").map { File(it) }
val renderDocUiPath = providers.gradleProperty("loom.renderDocUiPath").map { File(it) }

loom {
    mods.maybeCreate(modId).apply {
        sourceSet(sourceSets.getByName("main"))
    }

    runs {
        named("client") {
            jvmArguments.addAll(Constants.JVM_ARGUMENTS)
            systemProperties.putAll(Constants.SYSTEM_PROPERTIES)
            programArguments.addAll(Constants.PROGRAM_ARGUMENTS)
        }

        if (tracyCapturePath.isPresent) {
            create("clientTracy") {
                inherit(getByName("client"))
            }
        }

        // We're a client mod, but we need to make sure we correctly render when playing on a server.
        named("server")

        configureEach {
            generateRunConfig = true
            preferGradleTask = true

            displayName = "$modName Fabric ${Strings.capitalizeCamelCaseName(name)}"
            ideConfigFolder = "Fabric"
        }
    }
}

tasks {
    if (tracyCapturePath.isPresent) {
        named<RunGameTask>("runClientTracy").configure {
            tracy {
                tracyCapture.set(tracyCapturePath.get())
                output.set(workingDir.resolve("profile.tracy"))
            }
        }
    }

    if (renderDocCliPath.isPresent) {
        withType<RenderDocRunTask>().configureEach {
            renderDocExecutable.set(renderDocCliPath.get())
        }
    }

    if (renderDocUiPath.isPresent) {
        withType<RenderDocRunUITask>().configureEach {
            renderDocExecutable.set(renderDocUiPath.get())
        }
    }
}

dependencies {
    minecraft(libraryOf("minecraft"))
}
