package com.jozufozu.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class PackageInfosPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        target.extensions.create('defaultPackageInfos', PackageInfosExtension, target)
    }
}

