package com.jozufozu.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the

open class TransitiveSourceSetsExtension(private val project: Project) {
    var compileClasspath: FileCollection? = null
    var runtimeClasspath: FileCollection? = null

    private val transitives = mutableMapOf<SourceSet, TransitiveSourceSetConfigurator>()

    fun create(name: String) {
        sourceSet(project.the<SourceSetContainer>().maybeCreate(name))
    }

    fun create(name: String, action: Action<TransitiveSourceSetConfigurator>) {
        sourceSet(project.the<SourceSetContainer>().maybeCreate(name), action)
    }

    fun sourceSet(name: String) {
        sourceSet(project.the<SourceSetContainer>().getByName(name))
    }

    fun sourceSet(name: String, action: Action<TransitiveSourceSetConfigurator>) {
        sourceSet(project.the<SourceSetContainer>().getByName(name), action)
    }

    fun sourceSet(sourceSet: SourceSet) {
        registerSourceSet(sourceSet)
    }

    fun sourceSet(sourceSet: SourceSet, action: Action<TransitiveSourceSetConfigurator>) {
        action.execute(registerSourceSet(sourceSet))
    }

    private fun registerSourceSet(sourceSet: SourceSet): TransitiveSourceSetConfigurator {
        return transitives.computeIfAbsent(sourceSet) { TransitiveSourceSetConfigurator(this, it) }
    }

    fun createCompileConfigurations() {
        val configs = transitives.mapValues { (sourceSet, _) ->
            project.configurations.create("for${sourceSet.name.capitalize()}") {
                isCanBeConsumed = true
                isCanBeResolved = false
            }
        }

        transitives.forEach { (sourceSet, configurator) ->
            project.configurations.named(sourceSet.compileOnlyConfigurationName).configure {
                extendsFrom(configs[sourceSet])
                configurator.compileSourceSets.forEach {
                    extendsFrom(configs[it])
                }
            }
        }
    }

    fun createRuntimeConfigurations() {
        val configs = transitives.mapValues { (sourceSet, _) ->
            project.configurations.create("run${sourceSet.name.capitalize()}") {
                isCanBeConsumed = true
                isCanBeResolved = false
            }
        }

        transitives.forEach { (sourceSet, configurator) ->
            project.configurations.named(sourceSet.runtimeOnlyConfigurationName).configure {
                extendsFrom(configs[sourceSet])
                configurator.runtimeSourceSets.forEach {
                    extendsFrom(configs[it])
                }
            }
        }
    }
}
