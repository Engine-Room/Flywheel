package com.jozufozu.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet

@CompileStatic
class PackageInfosExtension {
    final Project project

    PackageInfosExtension(Project project) {
        this.project = project
    }

    void forSourceSets(SourceSet... sourceSets) {
        for (SourceSet sourceSet : sourceSets) {
            _forSourceSet(sourceSet)
        }
    }

    private void _forSourceSet(SourceSet sourceSet) {
        // We have to capture the source set name for the lazy string literals,
        // otherwise it'll just be whatever the last source set is in the list.
        def sourceSetName = sourceSet.name
        def taskName = sourceSet.getTaskName('generate', 'PackageInfos')
        def task = project.tasks.register(taskName, GeneratePackageInfosTask) {
            it.group = 'flywheel'
            it.description = "Generates package-info files for $sourceSetName packages."

            // Only apply to default source directory since we also add the generated
            // sources to the source set.
            it.sourceRoot.set(project.file("src/$sourceSetName/java"))
            it.outputDir.set(project.file("src/$sourceSetName/generatedPackageInfos"))
        }
        sourceSet.java.srcDir(task)

        project.tasks.named('ideaSyncTask').configure {
            it.finalizedBy(task)
        }

        def cleanTask = project.tasks.register(sourceSet.getTaskName('clean', 'PackageInfos'), Delete) {
            it.group = 'flywheel'
            it.delete(project.file("src/$sourceSetName/generatedPackageInfos"))
        }
        project.tasks.named('clean').configure {
            it.dependsOn(cleanTask)
        }
    }
}
