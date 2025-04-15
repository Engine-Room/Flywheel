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
        compileClasspath(project(platform), "api", "lib", "main")

        bundleFrom(project(common), "vanillin")
    }
}

val replaceProperties = listOf(
    "mod_license",
    "mod_sources",
    "mod_issues",
    "mod_homepage",
    "flywheel_id",
    "vanillin_id",
    "vanillin_name",
    "vanillin_description",
    "flywheel_maven_version_range",
    "minecraft_maven_version_range",
    "neoforge_version_range",
).associateWith { property(it) as String }
    .plus("vanillin_version" to "${property("vanillin_version")}${if (subproject.buildNumber != null) "-${subproject.buildNumber}" else ""}")

tasks.withType<ProcessResources>().configureEach {
    inputs.properties(replaceProperties)

    filesMatching(listOf("pack.mcmeta", "META-INF/neoforge.mods.toml")) {
        expand(replaceProperties)
    }
}

jarSets {
    mainSet.publishWithRawSources {
        artifactId = "vanillin-neoforge-${property("artifact_minecraft_version")}"
    }
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

    compileOnly(project(path = platform, configuration = "apiClasses"))

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    // JiJ flywheel proper
    include(project(path = platform, configuration = "flywheelRemap"))
    runtimeOnly(project(path = platform, configuration = "flywheelDev"))
}
