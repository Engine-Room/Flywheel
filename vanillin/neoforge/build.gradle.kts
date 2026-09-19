plugins {
    alias(libs.plugins.flywheel.gradle)
    alias(libs.plugins.configure.base)
    alias(libs.plugins.configure.neoforge)
    alias(libs.plugins.setup.repositories)
}

val vanillinCommon = projects.vanillin.vanillinCommon.path
val platform = projects.flywheelNeoforge.path

val main = sourceSets.getByName("main")

transitiveSourceSets {
    sourceSet(main) {
        compileClasspath(project(platform), "api", "lib", "main")

        bundleFrom(project(vanillinCommon))
    }
}

defaultPackageInfos {
    sources(main)
}

dependencies {
    compileOnly(libs.sodium.neoforge.api)

    compileOnly(project(path = vanillinCommon, configuration = "mainClasses"))
    compileOnly(project(path = vanillinCommon, configuration = "mainResources"))

    compileOnly(project(path = platform, configuration = "apiClasses"))

    jarJar(runtimeOnly(project(path = platform, configuration = "flywheel"))!!)
}
