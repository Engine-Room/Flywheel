package com.jozufozu.gradle

import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar

class JarTaskSet {
    TaskProvider<Jar> jar
    TaskProvider<RemapJarTask> remapJar
    TaskProvider<Jar> sources
    TaskProvider<RemapSourcesJarTask> remapSources
    TaskProvider<Jar> javadocJar

    JarTaskSet(TaskProvider<Jar> jar, TaskProvider<RemapJarTask> remapJar, TaskProvider<Jar> sources, TaskProvider<RemapSourcesJarTask> remapSources, TaskProvider<Jar> javadocJar) {
        this.jar = jar
        this.remapJar = remapJar
        this.sources = sources
        this.remapSources = remapSources
        this.javadocJar = javadocJar
    }
}
