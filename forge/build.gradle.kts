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
    mainSet.publish(platform.modArtifactId)
    create("api", api, lib).apply {
        addToAssemble()
        publish(platform.apiArtifactId)

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

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
    implementation(include("io.github.llamalad7:mixinextras-forge:0.4.1")!!)

    "forApi"(project(path = ":common", configuration = "commonApiOnly"))
    "forLib"(project(path = ":common", configuration = "commonLib"))
    "forBackend"(project(path = ":common", configuration = "commonBackend"))
    "forMain"(project(path = ":common", configuration = "commonImpl"))
}
