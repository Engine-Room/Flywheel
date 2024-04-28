package com.jozufozu.gradle


import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class JarSetPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('jarSets', JarSetExtension, project)
    }
}

