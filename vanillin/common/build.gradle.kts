plugins {
    alias(libs.plugins.flywheel.gradle)
    alias(libs.plugins.configure.base)
    alias(libs.plugins.configure.common)
    alias(libs.plugins.setup.repositories)
}

val common = projects.flywheelCommon.path

val main = sourceSets.getByName("main")

transitiveSourceSets {
    sourceSet(main) {
        compileClasspath(project(common), "api", "lib", "main")
        outgoing()
    }
}

defaultPackageInfos {
    sources(main)
}

dependencies {
    compileOnly(libs.bundles.mixin)
    annotationProcessor(libs.mixin.extras)

    compileOnly(libs.sodium.fabric.api)
}
