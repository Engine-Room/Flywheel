package com.jozufozu.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files

// Adapted from https://github.com/FabricMC/fabric/blob/31787236d242247e0b6c4ae806b1cfaa7042a62c/gradle/package-info.gradle, which is licensed under Apache 2.0.
class GeneratePackageInfosTask extends DefaultTask {
    @SkipWhenEmpty
    @InputDirectory
    final DirectoryProperty sourceRoot = project.objects.directoryProperty()

    @OutputDirectory
    final DirectoryProperty outputDir = project.objects.directoryProperty()

    @TaskAction
    def run() {
        def output = outputDir.get().asFile.toPath()
        output.deleteDir()
        def root = sourceRoot.get().asFile.toPath()

        root.eachDirRecurse {
            def containsJava = Files.list(it).any {
                Files.isRegularFile(it) && it.fileName.toString().endsWith('.java')
            }

            if (containsJava && Files.notExists(it.resolve('package-info.java'))) {
                def relativePath = root.relativize(it)
                def target = output.resolve(relativePath)
                Files.createDirectories(target)

                target.resolve('package-info.java').withWriter {
                    def packageName = relativePath.toString().replace(File.separator, '.')
                    it.write("""@ParametersAreNonnullByDefault
					|@FieldsAreNonnullByDefault
					|@MethodsReturnNonnullByDefault
					|package $packageName;
					|
					|import javax.annotation.ParametersAreNonnullByDefault;
					|
					|import net.minecraft.FieldsAreNonnullByDefault;
					|import net.minecraft.MethodsReturnNonnullByDefault;
					|""".stripMargin())
                }
            }
        }
    }
}
