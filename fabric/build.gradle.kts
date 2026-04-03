plugins {
    idea
    java
    `maven-publish`
    alias(libs.plugins.loom)
    id("flywheel.subproject")
    id("flywheel.platform")
}

val common = ":common"
val commonProject = project(common)

subproject.init("flywheel-fabric", "flywheel_group", "flywheel_version")

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
        // Don't want stubs at runtime
        compileClasspath(stubs)
        implementation(api, lib, backend)

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

var flywheelVersion = "${property("flywheel_version")}+${libs.versions.minecraft.get()}"

if (subproject.buildNumber != null) {
    flywheelVersion += ".build.${subproject.buildNumber}"
}

val replaceProperties = listOf(
    "mod_license",
    "mod_sources",
    "mod_issues",
    "mod_homepage",
    "flywheel_id",
    "flywheel_name",
    "flywheel_description",
    "minecraft_semver_version_range",
    "fabric_api_version_range",
).associateWith { property(it) as String }
    .plus("flywheel_version" to flywheelVersion)

tasks.withType<ProcessResources>().configureEach {
    inputs.properties(replaceProperties)

    filesMatching(listOf("fabric.mod.json")) {
        expand(replaceProperties)
    }
}

loom {
    accessWidenerPath = file("src/main/resources/flywheel.accesswidener")
}

jarSets {
    mainSet.publishWithRawSources {
        artifactId = "flywheel-fabric-${property("artifact_minecraft_version")}"
    }
    mainSet.outgoing("flywheel")

    create("api", api, lib).apply {
        addToAssemble()
        publishWithRawSources {
            artifactId = "flywheel-fabric-api-${property("artifact_minecraft_version")}"
        }
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    api(libs.fabric.api)

    compileOnly(libs.sodium.fabric.api)
    compileOnly("maven.modrinth:iris:${libs.versions.iris.get()}-fabric")

    "forApi"(project(path = common, configuration = "apiClasses"))
    "forLib"(project(path = common, configuration = "libClasses"))
    "forBackend"(project(path = common, configuration = "backendClasses"))
    "forStubs"(project(path = common, configuration = "stubsClasses"))
    "forMain"(project(path = common, configuration = "mainClasses"))

    "forLib"(project(path = common, configuration = "libResources"))
    "forBackend"(project(path = common, configuration = "backendResources"))
    "forMain"(project(path = common, configuration = "mainResources"))
}
