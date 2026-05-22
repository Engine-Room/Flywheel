package dev.engine_room.gradle.platform

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RenderDocRunTask
import net.fabricmc.loom.task.RenderDocRunUITask
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import java.io.File

open class PlatformExtension(val project: Project) {
    fun setupLoomMod(vararg sourceSets: SourceSet) {
        project.the<LoomGradleExtensionAPI>().mods.maybeCreate("main").apply {
            sourceSets.forEach(::sourceSet)
        }
    }

    fun setupMdgMod(vararg sourceSets: SourceSet) {
        project.the<ModDevExtension>().mods.maybeCreate("main").apply {
            sourceSets.forEach(::sourceSet)
        }
    }

    fun setupLoomRuns() {
        project.the<LoomGradleExtensionAPI>().runs.apply {
            named("client") {
                isIdeConfigGenerated = true

                // Better hotswap, only available with the JetBrains Runtime so we have to ignore invalid options aswell
                jvmArguments.add("-XX:+IgnoreUnrecognizedVMOptions")
                jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")

                // Turn on our own debug flags
                property("flw.dumpShaderSource", "true")
                property("flw.debugMemorySafety", "true")

                // Turn on mixin debug flags
                property("mixin.debug.export", "true")
                property("mixin.debug.verbose", "true")

                programArgs("--renderDebugLabels")

                // 720p baby!
                programArgs("--width", "1280", "--height", "720")
            }

            // We're a client mod, but we need to make sure we correctly render when playing on a server.
            named("server") {
                isIdeConfigGenerated = true
                programArgs("--nogui")
            }
        }


        if (System.getProperty("os.name") == "Linux") {
            project.tasks.withType<RenderDocRunTask> {
                renderDocExecutable.set(File("/usr/bin/renderdoccmd"))
            }

            project.tasks.withType<RenderDocRunUITask> {
                renderDocExecutable.set(File("/usr/bin/qrenderdoc"))
            }
        }
    }

    fun setupMdgRuns() {
        project.the<ModDevExtension>().runs.apply {
            create("client") {
                client()

                // Better hotswap, only available with the JetBrains Runtime so we have to ignore invalid options aswell
                jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
                jvmArgument("-XX:+AllowEnhancedClassRedefinition")

                // Turn on our own debug flags
                systemProperty("flw.dumpShaderSource", "true")
                systemProperty("flw.debugMemorySafety", "true")

                // Turn on mixin debug flags
                systemProperty("mixin.debug.export", "true")
                systemProperty("mixin.debug.verbose", "true")

                // 720p baby!
                programArguments.addAll("--width", "1280", "--height", "720")

                // Helpful when debugging issues
                programArguments.addAll("--renderDebugLabels", "--vulkanValidation")
            }

            // We're a client mod, but we need to make sure we correctly render when playing on a server.
            create("server") {
                server()

                programArgument("--nogui")
            }
        }
    }

    fun setupTestMod(sourceSet: SourceSet) {
        project.tasks.apply {
            val testModJar = register<Jar>("testModJar") {
                from(sourceSet.output)
                archiveClassifier = "testmod"
            }

            named<Task>("build").configure {
                dependsOn(testModJar)
            }
        }
    }
}
