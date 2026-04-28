plugins {
    idea
    java
    `maven-publish`
    alias(libs.plugins.mdg)
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
    setupMdgMod(api, lib, backend, main)
    setupMdgRuns()
    setupTestMod(testMod)
}

val replaceProperties = listOf(
    "mod_license",
    "mod_sources",
    "mod_issues",
    "mod_homepage",
    "flywheel_id",
    "flywheel_name",
    "flywheel_description",
    "minecraft_maven_version_range",
    "neoforge_version_range",
).associateWith { property(it) as String }
    .plus("flywheel_version" to "${property("flywheel_version")}${if (subproject.buildNumber != null) "-${subproject.buildNumber}" else ""}")

tasks.withType<ProcessResources>().configureEach {
    inputs.properties(replaceProperties)

    filesMatching(listOf("pack.mcmeta", "META-INF/neoforge.mods.toml")) {
        expand(replaceProperties)
    }
}

jarSets {
    mainSet.publishWithRawSources {
        artifactId = "flywheel-neoforge-${property("artifact_minecraft_version")}"
    }
    mainSet.outgoing("flywheel")

    create("api", api, lib).apply {
        addToAssemble()
        publishWithRawSources {
            artifactId = "flywheel-neoforge-api-${property("artifact_minecraft_version")}"
        }
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
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
        create("flywheel") {
            sourceSet(api)
            sourceSet(lib)
            sourceSet(backend)
            sourceSet(main)
        }
    }
}

dependencies {
    compileOnly(libs.sodium.neoforge.api)
    compileOnly("maven.modrinth:iris:${libs.versions.iris.get()}-neoforge")

    "forApi"(project(path = common, configuration = "apiClasses"))
    "forLib"(project(path = common, configuration = "libClasses"))
    "forBackend"(project(path = common, configuration = "backendClasses"))
    "forStubs"(project(path = common, configuration = "stubsClasses"))
    "forMain"(project(path = common, configuration = "mainClasses"))

    "forLib"(project(path = common, configuration = "libResources"))
    "forBackend"(project(path = common, configuration = "backendResources"))
    "forMain"(project(path = common, configuration = "mainResources"))
}
