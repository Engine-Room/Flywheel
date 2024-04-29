package com.jozufozu.gradle

import com.jozufozu.gradle.jarset.JarTaskSet
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.the
import org.gradle.language.jvm.tasks.ProcessResources

class PlatformPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val commonProject = project.project(":common")
        val commonSourceSets = commonProject.the<SourceSetContainer>()

        val sourceSets = project.the<SourceSetContainer>()
        val loom = project.the<LoomGradleExtensionAPI>()
        val publishing = project.the<PublishingExtension>()

        val platformImpl = sourceSets.named("main").get()
        val platformApi = sourceSets.create("api")
        val platformLib = sourceSets.create("lib")
        val platformBackend = sourceSets.create("backend")

        // This is needed for both platforms.
        val mainMod = loom.mods.maybeCreate("main")
        mainMod.sourceSet(platformApi)
        mainMod.sourceSet(platformLib)
        mainMod.sourceSet(platformBackend)
        mainMod.sourceSet(platformImpl)

        val commonApi = commonSourceSets.named("api").get()
        val commonLib = commonSourceSets.named("lib").get()
        val commonBackend = commonSourceSets.named("backend").get()
        val commonImpl = commonSourceSets.named("main").get()

        val commonSources = listOf(commonApi, commonLib, commonBackend, commonImpl)

        // Directly compile the platform sources with the common sources
        includeFromCommon(project, platformApi, commonApi)
        includeFromCommon(project, platformLib, commonLib)
        includeFromCommon(project, platformBackend, commonBackend)
        includeFromCommon(project, platformImpl, commonImpl)

        val tasks = project.tasks

        tasks.withType(JavaCompile::class.java).configureEach {
            JarTaskSet.excludeDuplicatePackageInfos(this)
        }

        tasks.named("jar", Jar::class.java).configure {
            from(platformApi.output, platformLib.output, platformBackend.output)

            JarTaskSet.excludeDuplicatePackageInfos(this)
        }

        tasks.named("javadoc", Javadoc::class.java).configure {
            commonSources.forEach { source(it.allJava) }

            source(platformApi.allJava, platformLib.allJava, platformBackend.allJava)

            JarTaskSet.excludeDuplicatePackageInfos(this)
        }

        tasks.named("sourcesJar", Jar::class.java).configure {
            commonSources.forEach { from(it.allJava) }

            from(platformApi.allJava, platformLib.allJava, platformBackend.allJava)

            JarTaskSet.excludeDuplicatePackageInfos(this)
        }

        val remapJar = tasks.named("remapJar", RemapJarTask::class.java)
        val remapSourcesJar = tasks.named("remapSourcesJar", RemapSourcesJarTask::class.java)
        val javadocJar = tasks.named("javadocJar", Jar::class.java)

        val apiSet = JarTaskSet.create(project, "api", platformApi, platformLib)

        val mcVersion = project.property("artifact_minecraft_version")

        publishing.publications {
            // we should be using remapped on both Fabric and Forge because Forge needs to put things in srg
            register("mavenApi", MavenPublication::class.java) {
                artifact(apiSet.remapJar)
                artifact(apiSet.remapSources)
                artifact(apiSet.javadocJar)
                artifactId = "flywheel-${project.name}-api-${mcVersion}"
            }
            register("mavenImpl", MavenPublication::class.java) {
                artifact(remapJar)
                artifact(remapSourcesJar)
                artifact(javadocJar)
                artifactId = "flywheel-${project.name}-${mcVersion}"
            }
        }
    }

    private fun includeFromCommon(project: Project, sourceSet: SourceSet, commonSourceSet: SourceSet) {
        project.tasks.named(sourceSet.compileJavaTaskName, JavaCompile::class.java).configure {
            source(commonSourceSet.allJava)
        }

        project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources::class.java).configure {
            from(commonSourceSet.resources)
        }
    }
}
