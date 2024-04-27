package com.jozufozu.gradle

import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

class JarTaskUtils {
    // We have duplicate packages between the common and platform dependent subprojects.
    // In theory the package-info.java files should be identical, so just take the first one we find.
    static void excludeDuplicatePackageInfos(AbstractCopyTask copyTask) {
        copyTask.filesMatching('**/package-info.java') { FileCopyDetails details ->
            details.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }

    // The compile/javadoc tasks have a different base type that isn't so smart about exclusion handling.
    static void excludeDuplicatePackageInfos(SourceTask sourceTask) {
        sourceTask.exclude('**/package-info.java')
    }

    static JarTaskSet createJarAndOutgoingConfiguration(Project project, String name, SourceSet... sourceSetSet) {
        def config = project.configurations.register("common${name.capitalize()}") {
            canBeConsumed = true
            canBeResolved = false
        }
        def jarTask = project.tasks.register("${name}Jar", Jar) {
            group = 'Build'
            archiveClassifier.set(name)
            destinationDirectory.set(project.layout.buildDirectory.dir('devlibs'))
            for (SourceSet set in sourceSetSet) {
                from set.output
            }
            excludeDuplicatePackageInfos(it)
        }
        def remapJarTask = project.tasks.register("${name}RemapJar", RemapJarTask) {
            group = 'Loom'
            dependsOn(jarTask)
            archiveClassifier.set(name)
            inputFile.set(jarTask.flatMap { it.archiveFile })
        }
        def sourcesTask = project.tasks.register("${name}SourcesJar", Jar) {
            group = 'Build'
            archiveClassifier.set("${name}-sources")
            destinationDirectory.set(project.layout.buildDirectory.dir('devlibs'))
            for (SourceSet set in sourceSetSet) {
                from set.allSource
            }
            excludeDuplicatePackageInfos(it)
        }
        def remapSourcesTask = project.tasks.register("${name}RemapSourcesJar", RemapSourcesJarTask) {
            group = 'Loom'
            dependsOn(sourcesTask)
            archiveClassifier.set("${name}-sources")
            inputFile.set(sourcesTask.flatMap { it.archiveFile })
        }
        def javadocTask = project.tasks.register("${name}Javadoc", Javadoc) {
            group = 'Build'
            destinationDir = project.layout.buildDirectory.dir("docs/${name}-javadoc").get().asFile
            options.encoding = 'UTF-8'
            options.optionFiles(project.rootProject.file('javadoc-options.txt'))
            for (SourceSet set in sourceSetSet) {
                source set.allJava
                classpath += set.compileClasspath
            }
            excludeDuplicatePackageInfos(it)
        }
        def javadocJarTask = project.tasks.register("${name}JavadocJar", Jar) {
            group = 'Build'
            dependsOn(javadocTask)
            archiveClassifier.set("${name}-javadoc")
            destinationDirectory.set(project.layout.buildDirectory.dir('libs'))
            from(javadocTask.map { it.outputs })
        }

        project.artifacts.add(config.name, jarTask)

        project.tasks.named('assemble').configure {
            dependsOn(jarTask, remapJarTask, sourcesTask, remapSourcesTask, javadocTask, javadocJarTask)
        }

        return new JarTaskSet(jarTask, remapJarTask, sourcesTask, remapSourcesTask, javadocJarTask)
    }
}
