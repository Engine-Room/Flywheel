plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
}

subproject.init("flywheel-common", "flywheel_group", "flywheel_version")

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val stubs = sourceSets.create("stubs")
val main = sourceSets.getByName("main")
val vanillin = sourceSets.create("vanillin")

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
        compileClasspath(api, lib)
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
        publish("flywheel-common-intermediary-api-${property("artifact_minecraft_version")}")

        configureJar {
            manifest {
                attributes("Fabric-Loom-Remap" to "true")
            }
        }

        // Don't publish the un-remapped jars because they don't have the correct manifest populated by Loom.
        forkRemap("apiMojmap").apply {
            addToAssemble()
            configureRemap {
                // "named" == mojmap
                // We're probably remapping from named to named so Loom should noop this.
                targetNamespace = "named"
            }

            publish("flywheel-common-mojmap-api-${property("artifact_minecraft_version")}")
        }
    }
}

dependencies {
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")

    modCompileOnly("maven.modrinth:sodium:${property("sodium_version")}-fabric")
    modCompileOnly("maven.modrinth:iris:${property("iris_version")}-fabric")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
