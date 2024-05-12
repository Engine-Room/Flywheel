plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
}

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val main = sourceSets.getByName("main")

transitiveSourceSets {
    compileClasspath = main.compileClasspath

    sourceSet(api) {
        rootCompile()
    }
    sourceSet(lib) {
        rootCompile()
        compile(api)
    }
    sourceSet(backend) {
        rootCompile()
        compile(api, lib)
    }
    sourceSet(main) {
        compile(api, lib, backend)
    }
    sourceSet(sourceSets.getByName("test")) {
        implementation(api, lib, backend)
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

jarSets {
    // For sharing with other subprojects.
    outgoing("commonApiOnly", api)
    outgoing("commonLib", lib)
    outgoing("commonBackend", backend)
    outgoing("commonImpl", main)

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

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
