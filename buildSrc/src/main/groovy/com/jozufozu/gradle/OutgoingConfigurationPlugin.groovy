package com.jozufozu.gradle

import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

class OutgoingConfigurationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("outgoing", Extension, project)
    }

    static class Extension {
        private final Project project
        private final SourceSetContainer sourceSets
        private final ConfigurationContainer configurations
        private final TaskContainer tasks
        private final ArtifactHandler artifacts

        Extension(Project project) {
            this.project = project
            this.sourceSets = project.getExtensions().getByType(SourceSetContainer)
            this.configurations = project.configurations
            this.tasks = project.tasks
            this.artifacts = project.artifacts
        }

        JarTaskSet createJarAndOutgoingConfiguration(String name) {
            return createJarAndOutgoingConfiguration(name, sourceSets.named(name).get())
        }

        JarTaskSet createJarAndOutgoingConfiguration(String name, SourceSet... sourceSetSet) {
            def config = configurations.register("common${name.capitalize()}") {
                canBeConsumed = true
                canBeResolved = false
            }
            def jarTask = tasks.register("${name}Jar", Jar) {
                group = 'Build'
                archiveClassifier.set(name)
                destinationDirectory.set(project.layout.buildDirectory.dir('devlibs'))
                for (SourceSet set in sourceSetSet) {
                    from set.output
                }
            }
            def remapJarTask = tasks.register("${name}RemapJar", RemapJarTask) {
                group = 'Loom'
                dependsOn(jarTask)
                archiveClassifier.set(name)
                inputFile.set(jarTask.flatMap { it.archiveFile })
            }
            def sourcesTask = tasks.register("${name}SourcesJar", Jar) {
                group = 'Build'
                archiveClassifier.set("${name}-sources")
                destinationDirectory.set(project.layout.buildDirectory.dir('devlibs'))
                for (SourceSet set in sourceSetSet) {
                    from set.allSource
                }
            }
            def remapSourcesTask = tasks.register("${name}RemapSourcesJar", RemapSourcesJarTask) {
                group = 'Loom'
                dependsOn(sourcesTask)
                archiveClassifier.set("${name}-sources")
                inputFile.set(sourcesTask.flatMap { it.archiveFile })
            }
            def javadocTask = tasks.register("${name}Javadoc", Javadoc) {
                group = 'Build'
                destinationDir = project.layout.buildDirectory.dir("docs/${name}-javadoc").get().asFile
                options.encoding = 'UTF-8'
                for (SourceSet set in sourceSetSet) {
                    source set.allJava
                    classpath += set.compileClasspath
                }
            }
            def javadocJarTask = tasks.register("${name}JavadocJar", Jar) {
                group = 'Build'
                dependsOn(javadocTask)
                archiveClassifier.set("${name}-javadoc")
                destinationDirectory.set(project.layout.buildDirectory.dir('libs'))
                from(javadocTask.map { it.outputs })
            }

            artifacts.add(config.name, jarTask)

            tasks.named('assemble').configure {
                dependsOn(jarTask, remapJarTask, sourcesTask, remapSourcesTask, javadocTask, javadocJarTask)
            }

            return new JarTaskSet(jarTask, remapJarTask, sourcesTask, remapSourcesTask, javadocJarTask)
        }
    }
}
