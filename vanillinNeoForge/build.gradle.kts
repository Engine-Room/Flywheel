plugins {
    idea
    java
    `maven-publish`
    alias(libs.plugins.mdg)
    id("flywheel.subproject")
    id("flywheel.platform")
}

val common = ":common"
val platform = ":neoforge"

subproject.init("vanillin-neoforge", "vanillin_group", "vanillin_version")

val main = sourceSets.getByName("main")

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

neoForge {
    version = libs.versions.neoforge.get()

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "")
            systemProperty("forge.logging.console.level", "debug")
        }
    }

    mods {
        create("vanillin") {
            sourceSet(main)
        }
    }
}

dependencies {
    compileOnly(libs.sodium.neoforge.api)
    compileOnly("maven.modrinth:iris:${libs.versions.iris.get()}-neoforge")

    compileOnly(project(path = common, configuration = "vanillinClasses"))
    compileOnly(project(path = common, configuration = "vanillinResources"))

    compileOnly(project(path = platform, configuration = "apiClasses"))

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    jarJar(runtimeOnly(project(path = platform, configuration = "flywheel"))!!)
}
