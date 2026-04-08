plugins {
    idea
    java
    `maven-publish`
    alias(libs.plugins.loom)
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
        compileClasspath(project(platform), "api", "lib", "main")

        bundleFrom(project(common), "vanillin")
    }
}

var vanillinVersion = "${property("vanillin_version")}+${libs.versions.minecraft.get()}"

if (subproject.buildNumber != null) {
    vanillinVersion += ".build.${subproject.buildNumber}"
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
    "minecraft_semver_version_range",
    "flywheel_semver_version_range",
    "fabric_api_version_range",
).associateWith { property(it) as String }
    .plus("vanillin_version" to vanillinVersion)

tasks.withType<ProcessResources>().configureEach {
    inputs.properties(replaceProperties)

    filesMatching(listOf("fabric.mod.json")) {
        expand(replaceProperties)
    }
}

jarSets {
    mainSet.publishWithRawSources {
        artifactId = "vanillin-fabric-${property("artifact_minecraft_version")}"
    }
}

defaultPackageInfos {
    sources(main)
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    api(libs.fabric.api)

    compileOnly(libs.sodium.fabric.api)

    compileOnly(project(path = common, configuration = "vanillinClasses"))
    compileOnly(project(path = common, configuration = "vanillinResources"))

    compileOnly(project(path = platform, configuration = "apiClasses"))

    include(runtimeOnly(project(path = platform, configuration = "flywheel"))!!)
}
