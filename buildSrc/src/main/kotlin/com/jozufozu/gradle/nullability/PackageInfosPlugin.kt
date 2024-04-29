package com.jozufozu.gradle.nullability

import org.gradle.api.Plugin
import org.gradle.api.Project

class PackageInfosPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("defaultPackageInfos", PackageInfosExtension::class.java, target)
    }
}
