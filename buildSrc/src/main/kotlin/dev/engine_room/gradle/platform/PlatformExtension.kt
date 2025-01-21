package dev.engine_room.gradle.platform

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import java.io.File

open class PlatformExtension(val project: Project) {
    fun setupLoomMod(vararg sourceSets: SourceSet) {
        project.the<LoomGradleExtensionAPI>().mods.maybeCreate("main").apply {
            sourceSets.forEach(::sourceSet)
        }
    }

    fun setupLoomRuns() {
        project.the<LoomGradleExtensionAPI>().runs.apply {
            named("client") {
                isIdeConfigGenerated = true

                // Turn on our own debug flags
                property("flw.dumpShaderSource", "true")
                property("flw.debugMemorySafety", "true")

                // Turn on mixin debug flags
                property("mixin.debug.export", "true")
                property("mixin.debug.verbose", "true")

                // 720p baby!
                programArgs("--width", "1280", "--height", "720")
            }

            // We're a client mod, but we need to make sure we correctly render when playing on a server.
            named("server") {
                isIdeConfigGenerated = true
                programArgs("--nogui")
            }
        }
    }

    fun setupTestMod(sourceSet: SourceSet) {
        project.tasks.apply {
            val testModJar = register<Jar>("testModJar") {
                from(sourceSet.output)
                val file = File(project.layout.buildDirectory.asFile.get(), "devlibs");
                destinationDirectory.set(file)
                archiveClassifier = "testmod"
            }

            val remapTestModJar = register<RemapJarTask>("remapTestModJar") {
                dependsOn(testModJar)
                inputFile.set(testModJar.get().archiveFile)
                archiveClassifier = "testmod"
                addNestedDependencies = false
                classpath.from(sourceSet.compileClasspath)
            }

            named<Task>("build").configure {
                dependsOn(remapTestModJar)
            }
        }
    }
}
