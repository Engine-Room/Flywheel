package dev.engine_room.gradle.transitive

import org.gradle.api.tasks.SourceSet

class TransitiveSourceSetConfigurator(private val parent: TransitiveSourceSetsExtension, private val sourceSet: SourceSet) {
    internal val compileSourceSets = mutableSetOf<SourceSet>()
    internal val runtimeSourceSets = mutableSetOf<SourceSet>()

    fun rootCompile() {
        parent.compileClasspath?.let { sourceSet.compileClasspath = it }
    }

    fun rootRuntime() {
        parent.runtimeClasspath?.let { sourceSet.runtimeClasspath = it }
    }

    fun rootImplementation() {
        rootCompile()
        rootRuntime()
    }

    fun compile(vararg sourceSets: SourceSet) {
        compileSourceSets += sourceSets
        for (sourceSet in sourceSets) {
            this.sourceSet.compileClasspath += sourceSet.output
        }
    }

    fun runtime(vararg sourceSets: SourceSet) {
        runtimeSourceSets += sourceSets
        for (sourceSet in sourceSets) {
            this.sourceSet.runtimeClasspath += sourceSet.output
        }
    }

    fun implementation(vararg sourceSets: SourceSet) {
        compile(*sourceSets)
        runtime(*sourceSets)
    }
}
