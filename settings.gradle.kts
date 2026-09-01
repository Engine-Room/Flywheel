pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}

rootProject.name = "flywheel"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

rootDir.walkTopDown()
    .maxDepth(2)
    .filter { it.isDirectory }
    .filter { it != rootDir }
    .filter { it.name != "buildSrc" && it.name != "build-logic" }
    .filter { it.resolve("build.gradle").exists() || it.resolve("build.gradle.kts").exists() }
    .forEach {
        val relativePath = it.toRelativeString(rootDir)
            .replace(File.separatorChar, '-')
        val projectName = if (it.parentFile == rootDir) {
            ":${rootProject.name}-$relativePath"
        } else {
            ":${it.parentFile.name}:$relativePath"
        }

        include(projectName)
        project(projectName).projectDir = it
    }
