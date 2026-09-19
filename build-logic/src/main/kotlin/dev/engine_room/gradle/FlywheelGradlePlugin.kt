package dev.engine_room.gradle

import dev.engine_room.gradle.jarset.JarSetExtension
import dev.engine_room.gradle.nullability.PackageInfosExtension
import dev.engine_room.gradle.transitive.TransitiveSourceSetsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FlywheelGradlePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("defaultPackageInfos", PackageInfosExtension::class.java, project)
        project.extensions.create("transitiveSourceSets", TransitiveSourceSetsExtension::class.java, project)
        project.extensions.create("jarSets", JarSetExtension::class.java, project)
    }
}
