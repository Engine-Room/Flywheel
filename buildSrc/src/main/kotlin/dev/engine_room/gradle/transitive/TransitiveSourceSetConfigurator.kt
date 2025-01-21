package dev.engine_room.gradle.transitive

import dev.engine_room.gradle.jarset.JarTaskSet
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.the
import org.gradle.language.jvm.tasks.ProcessResources

class TransitiveSourceSetConfigurator(private val parent: TransitiveSourceSetsExtension, private val sourceSet: SourceSet) {
    internal val compileSourceSets = mutableSetOf<SourceSet>()
    internal val runtimeSourceSets = mutableSetOf<SourceSet>()

    fun rootCompile() {
        parent.compileClasspath?.let { sourceSet.compileClasspath = it }
    }

    fun rootRuntime() {
        parent.runtimeClasspath?.let { sourceSet.runtimeClasspath = it }
    }

    fun rootImplementation() {
        rootCompile()
        rootRuntime()
    }

    fun compileClasspath(vararg sourceSets: SourceSet) {
        compileSourceSets += sourceSets
        for (sourceSet in sourceSets) {
            this.sourceSet.compileClasspath += sourceSet.output
        }
    }

    fun compileClasspath(project: Project, vararg sourceSets: String) {
        val externalSourceSets = project.the<SourceSetContainer>()
        for (name in sourceSets) {
            this.sourceSet.compileClasspath += externalSourceSets.getByName(name).output
        }
    }

    fun runtimeClasspath(vararg sourceSets: SourceSet) {
        runtimeSourceSets += sourceSets
        for (sourceSet in sourceSets) {
            this.sourceSet.runtimeClasspath += sourceSet.output
        }
    }

    fun implementation(vararg sourceSets: SourceSet) {
        compileClasspath(*sourceSets)
        runtimeClasspath(*sourceSets)
    }

    fun from(otherProject: Project) {
        from(otherProject, sourceSet.name)
    }

    fun from(otherProject: Project, vararg names: String) {

        val otherSourceSets = otherProject.the<SourceSetContainer>()

        from(*names.map { otherSourceSets.getByName(it) }.toTypedArray())
    }

    fun from(vararg sourceSets: SourceSet) {
        parent.project.tasks.apply {
            named<JavaCompile>(sourceSet.compileJavaTaskName).configure {
                sourceSets.forEach { source(it.allJava) }

                JarTaskSet.excludeDuplicatePackageInfos(this)
            }
            named<ProcessResources>(sourceSet.processResourcesTaskName).configure {
                sourceSets.forEach { from(it.resources) }
            }
        }
    }

    fun bundleFrom(otherProject: Project) {
        bundleFrom(otherProject, sourceSet.name)
    }

    fun bundleFrom(otherProject: Project, vararg names: String) {
        val otherSourceSets = otherProject.the<SourceSetContainer>()

        bundleFrom(*names.map { otherSourceSets.getByName(it) }.toTypedArray())
    }

    fun bundleFrom(vararg sourceSets: SourceSet) {
        from(*sourceSets)
        // The external sourceSets will be included in the jar by default since we bring it into the java compile task,
        // however we need to make sure that the javadoc and sources jars also include the external sourceSets
        bundleJavadocAndSources(*sourceSets)
    }

    fun bundleOutput(vararg sourceSets: SourceSet) {
        bundleJavadocAndSources(*sourceSets)

        parent.project.tasks.apply {
            named<Jar>(sourceSet.jarTaskName).configure {
                sourceSets.forEach { from(it.output) }

                JarTaskSet.excludeDuplicatePackageInfos(this)
            }
        }
    }

    private fun bundleJavadocAndSources(vararg sourceSets: SourceSet) {
        parent.project.tasks.apply {
            named<Javadoc>(sourceSet.javadocTaskName).configure {
                sourceSets.forEach { source(it.allJava) }

                JarTaskSet.excludeDuplicatePackageInfos(this)
            }

            named<Jar>(sourceSet.sourcesJarTaskName).configure {
                sourceSets.forEach { from(it.allJava) }

                JarTaskSet.excludeDuplicatePackageInfos(this)
            }
        }
    }

    fun outgoing() {
        outgoingClasses()
        outgoingResources()
    }

    fun outgoingResources() {
        val project = parent.project
        val exportResources = project.configurations.register("${sourceSet.name}Resources") {
            isCanBeResolved = false
            isCanBeConsumed = true
        }
        val processResources = project.tasks.named<ProcessResources>(sourceSet.processResourcesTaskName).get()

        project.artifacts.add(exportResources.name, processResources.destinationDir) {
            builtBy(processResources)
        }
    }

    fun outgoingClasses() {
        val project = parent.project
        val exportClasses = project.configurations.register("${sourceSet.name}Classes") {
            isCanBeResolved = false
            isCanBeConsumed = true
        }

        val compileTask = project.tasks.named<JavaCompile>(sourceSet.compileJavaTaskName).get()
        project.artifacts.add(exportClasses.name, compileTask.destinationDirectory) {
            builtBy(compileTask)
        }
    }
}
