package com.jozufozu.gradle.jarset

import org.gradle.api.Plugin
import org.gradle.api.Project

class JarSetPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("jarSets", JarSetExtension::class.java, target)
    }
}
