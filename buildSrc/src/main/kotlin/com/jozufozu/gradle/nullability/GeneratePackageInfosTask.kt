package com.jozufozu.gradle.nullability

import org.apache.groovy.nio.extensions.NioExtensions
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

// Adapted from https://github.com/FabricMC/fabric/blob/31787236d242247e0b6c4ae806b1cfaa7042a62c/gradle/package-info.gradle, which is licensed under Apache 2.0.
open class GeneratePackageInfosTask: DefaultTask() {
    @SkipWhenEmpty
    @InputDirectory
    val sourceRoot: DirectoryProperty = project.objects.directoryProperty()

    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun run() {
        val output = outputDir.get().asFile.toPath()
        NioExtensions.deleteDir(output)
        val root = sourceRoot.get().asFile.toPath()

        NioExtensions.eachDirRecurse(root, closureOf<Path> {
            val containsJava = Files.list(this).anyMatch {
                Files.isRegularFile(it) && it.fileName.toString().endsWith(".java")
            }

            if (containsJava && Files.notExists(resolve("package-info.java"))) {
                val relativePath = root.relativize(this)
                val target = output.resolve(relativePath)
                Files.createDirectories(target)

                NioExtensions.withWriter(target.resolve("package-info.java"), closureOf<BufferedWriter> {
                    val packageName = relativePath.toString().replace(File.separator, ".")
                    write(StringGroovyMethods.stripMargin("""@ParametersAreNonnullByDefault
					|@FieldsAreNonnullByDefault
					|@MethodsReturnNonnullByDefault
					|package $packageName;
					|
					|import javax.annotation.ParametersAreNonnullByDefault;
					|
					|import net.minecraft.FieldsAreNonnullByDefault;
					|import net.minecraft.MethodsReturnNonnullByDefault;
					|"""))
                })
            }
        })
    }
}
