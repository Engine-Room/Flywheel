package com.jozufozu.gradle

import groovy.transform.CompileStatic
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.language.jvm.tasks.ProcessResources

// Couldn't get imports for loom to work in the simple .gradle file, so upgraded this to a real plugin.
@CompileStatic
class PlatformPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def commonProject = project.project(':common')
        def commonSourceSets = commonProject.getExtensions().getByType(SourceSetContainer)

        def sourceSets = project.getExtensions().getByType(SourceSetContainer)
        def loom = project.getExtensions().getByType(LoomGradleExtensionAPI)
        def publishing = project.getExtensions().getByType(PublishingExtension)

        // Loom only populates mc stuff to the main source set,
        // so grab that here and use it for the others.
        // Note that the `+` operator does NOT perform a deep copy
        // of a FileCollection, so this object is shared between
        // the source sets and we should avoid mutating it.
        SourceSet platformImpl = sourceSets.named('main').get()
        FileCollection mcCompileClassPath = platformImpl.compileClasspath

        SourceSet platformApi = sourceSets.create('api')
        platformApi.compileClasspath = mcCompileClassPath

        SourceSet platformLib = sourceSets.create('lib')
        platformLib.compileClasspath = mcCompileClassPath + platformApi.output

        SourceSet platformBackend = sourceSets.create('backend')
        platformBackend.compileClasspath = mcCompileClassPath + platformApi.output + platformLib.output

        // Assign here rather than concatenate to avoid modifying the mcCompileClassPath FileCollection
        platformImpl.compileClasspath = mcCompileClassPath + platformApi.output + platformLib.output + platformBackend.output
        // This isn't necessary for forge but fabric needs to recognize each classpath entry from ModSettings.
        platformImpl.runtimeClasspath += platformApi.output + platformLib.output + platformBackend.output

        // This is needed for both platforms.
        def mainMod = loom.mods.maybeCreate('main')
        mainMod.sourceSet(platformApi)
        mainMod.sourceSet(platformLib)
        mainMod.sourceSet(platformBackend)
        mainMod.sourceSet(platformImpl)

        def forApi = newConfiguration(project, 'forApi').get()
        def forLib = newConfiguration(project, 'forLib').get()
        def forBackend = newConfiguration(project, 'forBackend').get()
        def forImpl = newConfiguration(project, 'forImpl').get()

        extendsFrom(project, platformApi.compileOnlyConfigurationName, forApi)
        extendsFrom(project, platformLib.compileOnlyConfigurationName, forApi, forLib)
        extendsFrom(project, platformBackend.compileOnlyConfigurationName, forApi, forLib, forBackend)
        extendsFrom(project, platformImpl.compileOnlyConfigurationName, forApi, forLib, forBackend, forImpl)

        SourceSet commonApi = commonSourceSets.named('api').get()
        SourceSet commonLib = commonSourceSets.named('lib').get()
        SourceSet commonBackend = commonSourceSets.named('backend').get()
        SourceSet commonImpl = commonSourceSets.named('main').get()

        def commonSources = [commonApi, commonLib, commonBackend, commonImpl]

        // Directly compile the platform sources with the common sources
        includeFromCommon(project, platformApi, commonApi)
        includeFromCommon(project, platformLib, commonLib)
        includeFromCommon(project, platformBackend, commonBackend)
        includeFromCommon(project, platformImpl, commonImpl)

        def tasks = project.tasks

        tasks.withType(JavaCompile).configureEach { JavaCompile compileJava ->
            JarTaskUtils.excludeDuplicatePackageInfos(compileJava)
        }

        tasks.named('jar', Jar).configure { Jar jar ->
            jar.from platformApi.output, platformLib.output, platformBackend.output

            JarTaskUtils.excludeDuplicatePackageInfos(jar)
        }

        tasks.named('javadoc', Javadoc).configure { Javadoc javadoc ->
            commonSources.forEach { javadoc.source it.allJava }

            javadoc.source platformApi.allJava, platformLib.allJava, platformBackend.allJava

            JarTaskUtils.excludeDuplicatePackageInfos(javadoc)
        }

        tasks.named('sourcesJar', Jar).configure { Jar jar ->
            commonSources.forEach { jar.from it.allJava }

            jar.from platformApi.allJava, platformLib.allJava, platformBackend.allJava

            JarTaskUtils.excludeDuplicatePackageInfos(jar)
        }

        def remapJar = tasks.named('remapJar', RemapJarTask)
        def remapSourcesJar = tasks.named('remapSourcesJar', RemapSourcesJarTask)
        def javadocJar = tasks.named('javadocJar', Jar)

        JarTaskSet apiSet = JarTaskSet.create(project, 'api', platformApi, platformLib)

        def mcVersion = project.property('artifact_minecraft_version')

        publishing.publications {
            // we should be using remapped on both Fabric and Forge because Forge needs to put things in srg
            it.register('mavenApi', MavenPublication) { MavenPublication pub ->
                pub.artifact(apiSet.remapJar)
                pub.artifact(apiSet.remapSources)
                pub.artifact(apiSet.javadocJar)

                pub.artifactId = "flywheel-${project.name}-api-${mcVersion}"
            }
            it.register('mavenImpl', MavenPublication) { MavenPublication pub ->
                pub.artifact(remapJar)
                pub.artifact(remapSourcesJar)
                pub.artifact(javadocJar)
                pub.artifactId = "flywheel-${project.name}-${mcVersion}"
            }
        }
    }

    static NamedDomainObjectProvider<Configuration> newConfiguration(Project project, String name) {
        return project.configurations.register(name) { Configuration it ->
            it.canBeConsumed = true
            it.canBeResolved = false
        }
    }

    static void extendsFrom(Project project, String name, Configuration... configurations) {
        project.configurations.named(name).configure {
            it.extendsFrom(configurations)
        }
    }

    static void includeFromCommon(Project project, SourceSet sourceSet, SourceSet commonSourceSet) {
        project.tasks.named(sourceSet.compileJavaTaskName, JavaCompile).configure { JavaCompile compileJava ->
            compileJava.source commonSourceSet.allJava
        }

        project.tasks.named(sourceSet.processResourcesTaskName, ProcessResources).configure { ProcessResources processResources ->
            processResources.from commonSourceSet.resources
        }
    }
}
