package com.jozufozu.gradle.jarset

import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate

open class JarSetExtension(private val project: Project) {
    fun create(name: String, vararg sourceSetSet: SourceSet): JarTaskSet {
        return JarTaskSet.create(project, name, *sourceSetSet)
    }

    fun outgoing(name: String, vararg sourceSetSet: SourceSet): JarTaskSet {
        return JarTaskSet.create(project, name, *sourceSetSet).also { it.createOutgoingConfiguration() }
    }

    val mainSet: JarTaskSet by lazy {
        val jarTask = project.tasks.named<Jar>("jar")
        val remapJarTask = project.tasks.named<RemapJarTask>("remapJar")
        val sourcesJarTask = project.tasks.named<Jar>("sourcesJar")
        val remapSourcesJarTask = project.tasks.named<RemapSourcesJarTask>("remapSourcesJar")
        val javadocJarTask = project.tasks.named<Jar>("javadocJar")

        JarTaskSet(project, "main", jarTask, sourcesJarTask, javadocJarTask, remapJarTask, remapSourcesJarTask)
    }
}
