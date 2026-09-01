import dev.engine_room.gradle.getExt

plugins {
    alias(libs.plugins.flywheel.gradle)
    alias(libs.plugins.configure.base)
    alias(libs.plugins.configure.fabric)
    alias(libs.plugins.setup.repositories)
}

val common = projects.flywheelCommon.path
val commonProject = project(common)

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val main = sourceSets.getByName("main")

transitiveSourceSets {
    compileClasspath = main.compileClasspath

    sourceSet(api) {
        rootCompile()

        from(commonProject)

        outgoingClasses()
    }
    sourceSet(lib) {
        rootCompile()
        compileClasspath(api)

        from(commonProject)
    }
    sourceSet(backend) {
        rootCompile()
        compileClasspath(api, lib)

        from(commonProject)
    }
    sourceSet(main) {
        implementation(api, lib, backend)

        bundleFrom(commonProject)

        bundleOutput(api, lib, backend)
    }

    createCompileConfigurations()
}

loom {
    accessWidenerPath = file("src/main/resources/flywheel.accesswidener")

    mods.getByName(getExt("mod_id")).apply {
        sourceSet(api)
        sourceSet(lib)
        sourceSet(backend)
    }
}

jarSets {
    mainSet.outgoing("flywheel")

    create("api", api, lib).apply {
        addToAssemble()
        publish()
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

dependencies {
    implementation(libs.fabric.loader)
    api(libs.fabric.api)

    compileOnly(libs.sodium.fabric.api)
    compileOnly(libs.iris.fabric)

    "forApi"(project(path = common, configuration = "apiClasses"))
    "forLib"(project(path = common, configuration = "libClasses"))
    "forBackend"(project(path = common, configuration = "backendClasses"))
    "forMain"(project(path = common, configuration = "mainClasses"))

    "forLib"(project(path = common, configuration = "libResources"))
    "forBackend"(project(path = common, configuration = "backendResources"))
    "forMain"(project(path = common, configuration = "mainResources"))
}
