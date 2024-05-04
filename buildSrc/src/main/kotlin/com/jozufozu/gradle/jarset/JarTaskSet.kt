package com.jozufozu.gradle.jarset

import net.fabricmc.loom.task.AbstractRemapJarTask
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

class JarTaskSet(
    val project: Project,
    val name: String,
    val jar: TaskProvider<Jar>,
    val remapJar: TaskProvider<RemapJarTask>,
    val sources: TaskProvider<Jar>,
    val remapSources: TaskProvider<RemapSourcesJarTask>,
    val javadocJar: TaskProvider<Jar>
) {

    fun publishRemap(artifactId: String) {
        project.the<PublishingExtension>().publications {
            register<MavenPublication>("${name}RemapMaven") {
                artifact(remapJar)
                artifact(remapSources)
                artifact(javadocJar)
                this.artifactId = artifactId
            }
        }
    }

    fun publish(artifactId: String) {
        project.the<PublishingExtension>().publications {
            register<MavenPublication>("${name}Maven") {
                artifact(jar)
                artifact(sources)
                artifact(javadocJar)
                this.artifactId = artifactId
            }
        }
    }

    fun createOutgoingConfiguration() {
        val config = project.configurations.register(name) {
            isCanBeConsumed = true
            isCanBeResolved = false
        }

        project.artifacts.add(config.name, jar)
    }

    fun assembleRemaps() {
        project.tasks.named("assemble").configure {
            dependsOn(remapJar, remapSources, javadocJar)
        }
    }

    fun configureEach(action: Action<Jar>) {
        jar.configure(action)
        sources.configure(action)
        javadocJar.configure(action)

        remapJar.configure(action)
        remapSources.configure(action)
    }

    fun configureRemap(action: Action<AbstractRemapJarTask>) {
        remapJar.configure(action)
        remapSources.configure(action)
    }

    companion object {
        private const val PACKAGE_INFOS_JAVA_PATTERN = "**/package-info.java"
        private const val BUILD_GROUP: String = "build"
        private const val LOOM_GROUP: String = "loom"
        private const val JAVADOC_CLASSIFIER: String = "javadoc"
        private const val SOURCES_CLASSIFIER: String = "sources"

        /**
         * We have duplicate packages between the common and platform dependent subprojects.
         * In theory the package-info.java files should be identical, so just take the first one we find.
         */
        fun excludeDuplicatePackageInfos(copyTask: AbstractCopyTask) {
            copyTask.filesMatching(PACKAGE_INFOS_JAVA_PATTERN) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }

        /**
         * The compile/javadoc tasks have a different base type that isn't so smart about exclusion handling.
         */
        fun excludeDuplicatePackageInfos(sourceTask: SourceTask) {
            sourceTask.exclude(PACKAGE_INFOS_JAVA_PATTERN)
        }

        fun create(project: Project, name: String, vararg sourceSetSet: SourceSet): JarTaskSet {
            val buildDirectory = project.layout.buildDirectory
            val devlibs = buildDirectory.dir("devlibs/${name}")
            val libs = buildDirectory.dir("libs/${name}")

            val jarTask = project.tasks.register<Jar>("${name}Jar") {
                group = BUILD_GROUP
                destinationDirectory.set(devlibs)

                for (set in sourceSetSet) {
                    from(set.output)
                }
                excludeDuplicatePackageInfos(this)
            }
            val remapJarTask = project.tasks.register<RemapJarTask>("${name}RemapJar") {
                dependsOn(jarTask)
                group = LOOM_GROUP
                destinationDirectory.set(libs)

                inputFile.set(jarTask.flatMap { it.archiveFile })
            }
            val sourcesTask = project.tasks.register<Jar>("${name}SourcesJar") {
                group = BUILD_GROUP
                destinationDirectory.set(devlibs)
                archiveClassifier.set(SOURCES_CLASSIFIER)

                for (set in sourceSetSet) {
                    from(set.allSource)
                }
                excludeDuplicatePackageInfos(this)
            }
            val remapSourcesTask = project.tasks.register<RemapSourcesJarTask>("${name}RemapSourcesJar") {
                dependsOn(sourcesTask)
                group = LOOM_GROUP
                destinationDirectory.set(libs)
                archiveClassifier.set(SOURCES_CLASSIFIER)

                inputFile.set(sourcesTask.flatMap { it.archiveFile })
            }
            val javadocTask = project.tasks.register<Javadoc>("${name}Javadoc") {
                group = BUILD_GROUP
                setDestinationDir(buildDirectory.dir("docs/${name}-javadoc").get().asFile)

                for (set in sourceSetSet) {
                    source(set.allJava)
                    classpath += set.compileClasspath
                }
                excludeDuplicatePackageInfos(this)
            }
            val javadocJarTask = project.tasks.register<Jar>("${name}JavadocJar") {
                dependsOn(javadocTask)
                group = BUILD_GROUP
                destinationDirectory.set(libs)
                archiveClassifier.set(JAVADOC_CLASSIFIER)

                from(javadocTask.map { it.outputs })
            }

            return JarTaskSet(project, name, jarTask, remapJarTask, sourcesTask, remapSourcesTask, javadocJarTask)
        }
    }
}
