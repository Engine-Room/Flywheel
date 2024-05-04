package com.jozufozu.gradle.platform

import org.gradle.api.Plugin
import org.gradle.api.Project

class PlatformPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("platform", PlatformExtension::class.java, project)
    }
}
