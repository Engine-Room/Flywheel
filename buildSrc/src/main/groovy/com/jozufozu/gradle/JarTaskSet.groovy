package com.jozufozu.gradle

import groovy.transform.CompileStatic
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

@CompileStatic
class JarTaskSet {
    public static final String BUILD_GROUP = 'build'
    public static final String LOOM_GROUP = 'loom'
    public static final String JAVADOC_CLASSIFIER = "javadoc"
    public static final String SOURCES_CLASSIFIER = "sources"

    Project project
    String name
    TaskProvider<Jar> jar
    TaskProvider<RemapJarTask> remapJar
    TaskProvider<Jar> sources
    TaskProvider<RemapSourcesJarTask> remapSources
    TaskProvider<Jar> javadocJar

    JarTaskSet(Project project, String name, TaskProvider<Jar> jar, TaskProvider<RemapJarTask> remapJar, TaskProvider<Jar> sources, TaskProvider<RemapSourcesJarTask> remapSources, TaskProvider<Jar> javadocJar) {
        this.project = project
        this.name = name
        this.jar = jar
        this.remapJar = remapJar
        this.sources = sources
        this.remapSources = remapSources
        this.javadocJar = javadocJar
    }

    void createOutgoingConfiguration(String prefix) {
        def config = project.configurations.register("${prefix}${name.capitalize()}") {
            it.canBeConsumed = true
            it.canBeResolved = false
        }

        project.artifacts.add(config.name, jar)
    }

    void configure(Action<JarTaskSet> action) {
        action.execute(this)
    }

    void configureEach(Action<? extends Jar> action) {
        jar.configure(action)
        sources.configure(action)
        javadocJar.configure(action)

        remapJar.configure(action as Action<? super RemapJarTask>)
        remapSources.configure(action as Action<? super RemapSourcesJarTask>)
    }

    static JarTaskSet create(Project project, String name, SourceSet... sourceSetSet) {
        def buildDirectory = project.layout.buildDirectory
        def devlibs = buildDirectory.dir("devlibs/${name}")
        def libs = buildDirectory.dir("libs/${name}")

        def jarTask = project.tasks.register("${name}Jar", Jar) {
            it.group = BUILD_GROUP
            it.destinationDirectory.set(devlibs)

            for (SourceSet set in sourceSetSet) {
                it.from set.output
            }
            JarTaskUtils.excludeDuplicatePackageInfos(it)
        }
        def remapJarTask = project.tasks.register("${name}RemapJar", RemapJarTask) {
            it.dependsOn(jarTask)
            it.group = LOOM_GROUP
            it.destinationDirectory.set(libs)

            it.inputFile.set(jarTask.flatMap { it.archiveFile })
        }
        def sourcesTask = project.tasks.register("${name}SourcesJar", Jar) {
            it.group = BUILD_GROUP
            it.destinationDirectory.set(devlibs)
            it.archiveClassifier.set(SOURCES_CLASSIFIER)

            for (SourceSet set in sourceSetSet) {
                it.from set.allSource
            }
            JarTaskUtils.excludeDuplicatePackageInfos(it)
        }
        def remapSourcesTask = project.tasks.register("${name}RemapSourcesJar", RemapSourcesJarTask) {
            it.dependsOn(sourcesTask)
            it.group = LOOM_GROUP
            it.destinationDirectory.set(libs)
            it.archiveClassifier.set(SOURCES_CLASSIFIER)

            it.inputFile.set(sourcesTask.flatMap { it.archiveFile })
        }
        def javadocTask = project.tasks.register("${name}Javadoc", Javadoc) {
            it.group = BUILD_GROUP
            it.destinationDir = buildDirectory.dir("docs/${name}-javadoc").get().asFile
            it.options.encoding = 'UTF-8'
            it.options.optionFiles(project.rootProject.file('javadoc-options.txt'))

            for (SourceSet set in sourceSetSet) {
                it.source set.allJava
                it.classpath += set.compileClasspath
            }
            JarTaskUtils.excludeDuplicatePackageInfos(it)
        }
        def javadocJarTask = project.tasks.register("${name}JavadocJar", Jar) {
            it.dependsOn(javadocTask)
            it.group = BUILD_GROUP
            it.destinationDirectory.set(libs)
            it.archiveClassifier.set(JAVADOC_CLASSIFIER)

            it.from(javadocTask.map { it.outputs })
        }

        project.tasks.named('assemble').configure {
            it.dependsOn(remapJarTask, remapSourcesTask, javadocJarTask)
        }

        return new JarTaskSet(project, name, jarTask, remapJarTask, sourcesTask, remapSourcesTask, javadocJarTask)
    }
}
