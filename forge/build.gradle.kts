plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
    id("flywheel.platform")
}

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val main = sourceSets.getByName("main")

transitiveSourceSets {
    compileClasspath = main.compileClasspath

    sourceSet(api) {
        rootCompile()
    }
    sourceSet(lib) {
        rootCompile()
        compile(api)
    }
    sourceSet(backend) {
        rootCompile()
        compile(api, lib)
    }
    sourceSet(main) {
        compile(api, lib, backend)
    }

    createCompileConfigurations()
}

platform {
    commonProject = project(":common")
    sources(api, lib, backend, main)
    compileWithCommonSourceSets()
    setupLoomMod()
    setupLoomRuns()
    setupFatJar()
}

jarSets {
    mainSet.publishRemap(platform.modArtifactId)
    create("api", api, lib).publishRemap(platform.apiArtifactId)
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

loom {
    forge {
        mixinConfig("flywheel.backend.mixins.json")
        mixinConfig("flywheel.impl.mixins.json")
        mixinConfig("flywheel.impl.sodium.mixins.json")
    }

    runs {
        configureEach {
            property("forge.logging.markers", "")
            property("forge.logging.console.level", "debug")
        }
    }
}

dependencies {
    forge("net.minecraftforge:forge:${property("minecraft_version")}-${property("forge_version")}")

    modCompileOnly("maven.modrinth:embeddium:${property("embeddium_version")}")
    modCompileOnly("maven.modrinth:oculus:${property("oculus_version")}")

    "forApi"(project(path = ":common", configuration = "commonApiOnly"))
    "forLib"(project(path = ":common", configuration = "commonLib"))
    "forBackend"(project(path = ":common", configuration = "commonBackend"))
    "forMain"(project(path = ":common", configuration = "commonImpl"))
}
