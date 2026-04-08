plugins {
    idea
    java
    `maven-publish`
    alias(libs.plugins.loom)
    id("flywheel.subproject")
}

subproject.init("flywheel-common", "flywheel_group", "flywheel_version")

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val stubs = sourceSets.create("stubs")
val main = sourceSets.getByName("main")
val vanillin = sourceSets.create("vanillin")

loom {
    accessWidenerPath = file("flywheel-common.accesswidener")
}

transitiveSourceSets {
    compileClasspath = main.compileClasspath

    sourceSet(api) {
        rootCompile()
        outgoingClasses()
    }
    sourceSet(lib) {
        rootCompile()
        compileClasspath(api)
        outgoing()
    }
    sourceSet(backend) {
        rootCompile()
        compileClasspath(api, lib)
        outgoing()
    }
    sourceSet(stubs) {
        rootCompile()
        outgoingClasses()
    }
    sourceSet(main) {
        compileClasspath(api, lib, backend)
        outgoing()
    }
    sourceSet(sourceSets.getByName("test")) {
        implementation(api, lib, backend)
    }
    sourceSet(vanillin) {
        rootCompile()
        compileClasspath(api, lib, main)
        outgoing()
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main, vanillin)
}

jarSets {
    // For publishing.
    create("api", api, lib).apply {
        addToAssemble()

        publishWithRawSources {
            artifactId = "flywheel-common-api-${property("artifact_minecraft_version")}"
        }
    }

    create("vanillin", vanillin).apply {
        addToAssemble()

        publishWithRawSources {
            artifactId = "vanillin-common-${property("artifact_minecraft_version")}"
            version = property("vanillin_version") as String
            groupId = property("vanillin_group") as String
        }
    }
}

repositories {
    maven("https://maven.caffeinemc.net/releases/")
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.bundles.mixin)

    compileOnly(libs.sodium.fabric.api)
    compileOnly("maven.modrinth:iris:${libs.versions.iris.get()}-fabric")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
