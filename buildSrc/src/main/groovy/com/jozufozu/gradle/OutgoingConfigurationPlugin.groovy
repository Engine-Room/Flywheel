package com.jozufozu.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

class OutgoingConfigurationPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("outgoing", Extension, project)
    }

    static class Extension {
        private final Project project

        Extension(Project project) {
            this.project = project
        }

        JarTaskSet createJarAndOutgoingConfiguration(String name) {
            return createJarAndOutgoingConfiguration(name, project.getExtensions().getByType(SourceSetContainer).named(name).get())
        }

        JarTaskSet createJarAndOutgoingConfiguration(String name, SourceSet... sourceSetSet) {
            return JarTaskUtils.createJarAndOutgoingConfiguration(project, name, sourceSetSet)
        }
    }
}
