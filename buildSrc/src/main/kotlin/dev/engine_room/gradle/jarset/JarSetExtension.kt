package dev.engine_room.gradle.jarset

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate

open class JarSetExtension(private val project: Project) {
    fun create(name: String, vararg sourceSetSet: SourceSet): JarTaskSet {
        return JarTaskSet.create(project, name, *sourceSetSet)
    }

    val mainSet: JarTaskSet by lazy {
        val jarTask = project.tasks.named<Jar>("jar")
        val sourcesJarTask = project.tasks.named<Jar>("sourcesJar")
        val javadocJarTask = project.tasks.named<Jar>("javadocJar")

        JarTaskSet(project, "main", jarTask, sourcesJarTask, javadocJarTask)
    }
}
