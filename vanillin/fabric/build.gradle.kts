plugins {
    alias(libs.plugins.flywheel.gradle)
    alias(libs.plugins.configure.base)
    alias(libs.plugins.configure.fabric)
    alias(libs.plugins.setup.repositories)
}

val vanillinCommon = projects.vanillin.vanillinCommon.path
val platform = projects.flywheelFabric.path

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
    implementation(libs.fabric.loader)
    api(libs.fabric.api)

    compileOnly(libs.sodium.fabric.api)

    compileOnly(project(path = vanillinCommon, configuration = "mainClasses"))
    compileOnly(project(path = vanillinCommon, configuration = "mainResources"))

    compileOnly(project(path = platform, configuration = "apiClasses"))

    include(runtimeOnly(project(path = platform, configuration = "flywheel"))!!)
}
