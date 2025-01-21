plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
    id("flywheel.platform")
}

val common = ":common"
val platform = ":fabric"

subproject.init("vanillin-fabric", "vanillin_group", "vanillin_version")

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
    mainSet.publish("vanillin-fabric-${project.property("artifact_minecraft_version")}")
}

defaultPackageInfos {
    sources(main)
}

loom {
    mixin {
        useLegacyMixinAp = true
        add(main, "vanillin.refmap.json")
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    modCompileOnly("maven.modrinth:sodium:${property("sodium_version")}-fabric")

    compileOnly(project(path = common, configuration = "vanillinClasses"))
    compileOnly(project(path = common, configuration = "vanillinResources"))

    // JiJ flywheel proper
    include(project(path = platform, configuration = "flywheelRemap"))
    runtimeOnly(project(path = platform, configuration = "flywheelDev"))
}
