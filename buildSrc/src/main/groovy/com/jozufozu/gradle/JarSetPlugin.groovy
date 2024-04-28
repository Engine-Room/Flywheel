package com.jozufozu.gradle


import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

@CompileStatic
class JarSetPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("jarSets", JarSetExtension, project)
    }
}

@CompileStatic
class JarSetExtension {
    private final Project project

    JarSetExtension(Project project) {
        this.project = project
    }

    JarTaskSet createJars(String name) {
        return createJars(name, project.getExtensions().getByType(SourceSetContainer).named(name).get())
    }

    JarTaskSet createJars(String name, SourceSet... sourceSetSet) {
        return JarTaskSet.create(project, name, sourceSetSet)
    }
}
