import dev.engine_room.gradle.getProperty
import dev.engine_room.gradle.versionOf

plugins {
    java
    `maven-publish`
}

val modId = parent!!.name
val modName = getProperty("${modId}_name")
val modVersion = getProperty("${modId}_version")
val modDescription = getProperty("${modId}_description")

val modLoader = project.name.split('-').last()

val buildNumber: Provider<String> = providers.environmentVariable("RELEASE")
    .map { it.contentEquals("false", true) }
    .flatMap { dev ->
        providers.environmentVariable("BUILD_NUMBER")
            .filter { dev }
    }

// Only return the first 2 components of the minecraft version, ignoring the hotfix component
val artifactMinecraftVersion = versionOf("minecraft")
    .split('.')
    .take(2)
    .joinToString(".")

val mavenBuildSuffix = buildNumber.map { "-$it" }.getOrElse("")
val semverBuildSuffix = buildNumber.map { "+build.${it}" }.getOrElse("")

ext["mod_id"] = modId
ext["mod_name"] = modName

group = "${getProperty("base_group")}.${modId}"
// ${1.0.0}-mc${26.1}${-9999}
version = "${modVersion}-mc${artifactMinecraftVersion}${mavenBuildSuffix}"

// ${1.0.0}${+build.9999 OR -9999}
val loaderModVersion = modVersion + if (modLoader == "fabric") semverBuildSuffix else mavenBuildSuffix

val authors = getProperty("mod_authors")
tasks.processResources {
    fun prop(name: String) = Pair(name, getProperty(name))
    fun versionProp(name: String) = Pair("${name}_version", versionOf(name))

    val properties = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to loaderModVersion,
        "mod_description" to modDescription,
        "mod_authors" to authors,
        "mod_authors_json" to authors.formatForJson(),
        prop("mod_homepage"),
        prop("mod_sources"),
        prop("mod_issues"),
        prop("mod_license"),
        versionProp("minecraft"),
        versionProp("fabric_api"),
        versionProp("fabric_loader"),
        versionProp("neoforge"),
        versionProp("java"),
        prop("flywheel_maven_version_range"),
        prop("flywheel_semver_version_range"),
    )

    inputs.properties(properties)

    filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
        expand(properties)
    }
}

tasks.withType<Jar> {
    from(rootProject.file("LICENSE.md"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(versionOf("java"))
    withSourcesJar()
}

publishing {
    repositories {
        val mavenUsername = providers.environmentVariable("MAVEN_USERNAME")
        val mavenPassword = providers.environmentVariable("MAVEN_PASSWORD")
        if (mavenUsername.isPresent && mavenPassword.isPresent) {
            maven {
                url = uri("https://maven.createmod.net")
                credentials {
                    username = mavenUsername.get()
                    password = mavenPassword.get()
                }
            }
        }

        mavenLocal()
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components.getByName("java"))
            suppressAllPomMetadataWarnings()
        }
    }
}

// trick to sneak multiple entries into a single placeholder in a JSON file.
// the file must be valid even with placeholders, so we can't just do something like this: [${placeholder}]
// instead, the placeholder is expected to be in a string, like this: ["${placeholder}"]
// this takes a string in the format 'a, b, c' and adds quotes, so the end result will be like this: a", "b", "c
// when filled into the placeholder, you get a valid list: ["a", "b", "c"]
fun String.formatForJson(): String = split(", ").joinToString("\", \"")
