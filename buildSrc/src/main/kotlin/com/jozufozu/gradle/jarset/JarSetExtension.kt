package com.jozufozu.gradle.jarset

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the

open class JarSetExtension(val project: Project) {
    fun createJars(name: String): JarTaskSet {
        return createJars(name, project.the<SourceSetContainer>().named(name).get())
    }

    fun createJars(name: String, vararg sourceSetSet: SourceSet): JarTaskSet {
        return JarTaskSet.create(project, name, *sourceSetSet)
    }
}
