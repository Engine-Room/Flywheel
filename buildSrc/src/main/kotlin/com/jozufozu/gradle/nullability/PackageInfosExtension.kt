package com.jozufozu.gradle.nullability

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet

open class PackageInfosExtension(private val project: Project) {
    fun forSourceSets(vararg sourceSets: SourceSet) {
        for (sourceSet in sourceSets) {
            forSourceSet(sourceSet)
        }
    }

    private fun forSourceSet(sourceSet: SourceSet) {
        // We have to capture the source set name for the lazy string literals,
        // otherwise it'll just be whatever the last source set is in the list.
        val sourceSetName = sourceSet.name
        val taskName = sourceSet.getTaskName("generate", "PackageInfos")
        val task = project.tasks.register(taskName, GeneratePackageInfosTask::class.java) {
            group = "flywheel"
            description = "Generates package-info files for $sourceSetName packages."

            // Only apply to default source directory since we also add the generated
            // sources to the source set.
            sourceRoot.set(project.file("src/$sourceSetName/java"))
            outputDir.set(project.file("src/$sourceSetName/generatedPackageInfos"))
        }
        sourceSet.java.srcDir(task)

        project.tasks.named("ideaSyncTask").configure {
            finalizedBy(task)
        }

        val cleanTask = project.tasks.register(sourceSet.getTaskName("clean", "PackageInfos"), Delete::class.java) {
            group = "flywheel"
            delete(project.file("src/$sourceSetName/generatedPackageInfos"))
        }
        project.tasks.named("clean").configure {
            dependsOn(cleanTask)
        }
    }
}
