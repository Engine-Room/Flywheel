plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
    id("flywheel.platform")
}

val common = ":common"
val commonProject = project(common)

subproject.init("flywheel-neoforge", "flywheel_group", "flywheel_version")

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val stubs = sourceSets.create("stubs")
val main = sourceSets.getByName("main")
val testMod = sourceSets.create("testMod")

transitiveSourceSets {
    compileClasspath = main.compileClasspath

    sourceSet(api) {
        rootCompile()

        from(commonProject)
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
    sourceSet(stubs) {
        rootCompile()

        from(commonProject)
    }
    sourceSet(main) {
        compileClasspath(api, lib, backend)

        bundleFrom(commonProject)

        bundleOutput(api, lib, backend)
    }
    sourceSet(testMod) {
        rootCompile()
    }

    createCompileConfigurations()
}

platform {
    setupLoomMod(api, lib, backend, main)
    setupLoomRuns()
    setupTestMod(testMod)
}

jarSets {
    mainSet.publish("flywheel-neoforge-${project.property("artifact_minecraft_version")}")
    mainSet.outgoing("flywheel")

    create("api", api, lib).apply {
        addToAssemble()
        publish("flywheel-neoforge-api-${project.property("artifact_minecraft_version")}")

        configureJar {
            manifest {
                attributes("Fabric-Loom-Remap" to "true")
            }
        }
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

loom {
    mixin {
        useLegacyMixinAp = true
        add(main, "flywheel.refmap.json")
        add(backend, "backend-flywheel.refmap.json")
    }

    runs {
        configureEach {
            property("forge.logging.markers", "")
            property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    neoForge("net.neoforged:neoforge:${property("neoforge_version")}")

    modCompileOnly("maven.modrinth:sodium:${property("sodium_version")}-neoforge")
    modCompileOnly("maven.modrinth:iris:${property("iris_version")}-neoforge")

    modCompileOnly("maven.modrinth:embeddium:${property("embeddium_version")}")

    "forApi"(project(path = common, configuration = "apiClasses"))
    "forLib"(project(path = common, configuration = "libClasses"))
    "forBackend"(project(path = common, configuration = "backendClasses"))
    "forStubs"(project(path = common, configuration = "stubsClasses"))
    "forMain"(project(path = common, configuration = "mainClasses"))

    "forLib"(project(path = common, configuration = "libResources"))
    "forBackend"(project(path = common, configuration = "backendResources"))
    "forMain"(project(path = common, configuration = "mainResources"))
}
