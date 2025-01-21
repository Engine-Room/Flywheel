plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
    id("flywheel.platform")
}

val common = ":common"
val platform = ":neoforge"

subproject.init("vanillin-neoforge", "vanillin_group", "vanillin_version")

val main = sourceSets.getByName("main")

platform {
    setupLoomRuns()
}

transitiveSourceSets {
    sourceSet(main) {
        compileClasspath(project(platform), "api", "lib")

        bundleFrom(project(common), "vanillin")
    }
}

jarSets {
    mainSet.publish("vanillin-neoforge-${project.property("artifact_minecraft_version")}")
}

defaultPackageInfos {
    sources(main)
}

loom {
    mixin {
        useLegacyMixinAp = true
        add(main, "vanillin.refmap.json")
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

    compileOnly(project(path = common, configuration = "vanillinClasses"))
    compileOnly(project(path = common, configuration = "vanillinResources"))

    // JiJ flywheel proper
    include(project(path = platform, configuration = "flywheelRemap"))
    runtimeOnly(project(path = platform, configuration = "flywheelDev"))
}
