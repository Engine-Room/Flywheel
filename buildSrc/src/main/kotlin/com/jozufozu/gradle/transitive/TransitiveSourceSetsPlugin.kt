package com.jozufozu.gradle.transitive

import org.gradle.api.Plugin
import org.gradle.api.Project

class TransitiveSourceSetsPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("transitiveSourceSets", TransitiveSourceSetsExtension::class.java, target)
    }
}
