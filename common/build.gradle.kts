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
        publishWithRemappedSources {
            artifactId = "flywheel-common-intermediary-api-${property("artifact_minecraft_version")}"
        }

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

            publishWithRawSources {
                artifactId = "flywheel-common-mojmap-api-${property("artifact_minecraft_version")}"
            }
        }
    }

    create("vanillin", vanillin).apply {
        addToAssemble()
        publishWithRemappedSources {
            artifactId = "vanillin-common-intermediary-${property("artifact_minecraft_version")}"
            version = property("vanillin_version") as String
            groupId = property("vanillin_group") as String
        }

        configureJar {
            manifest {
                attributes("Fabric-Loom-Remap" to "true")
            }
        }

        // Don't publish the un-remapped jars because they don't have the correct manifest populated by Loom.
        forkRemap("vanillinMojmap").apply {
            addToAssemble()
            configureRemap {
                // "named" == mojmap
                // We're probably remapping from named to named so Loom should noop this.
                targetNamespace = "named"
            }

            publishWithRawSources {
                artifactId = "vanillin-common-mojmap-${property("artifact_minecraft_version")}"
                version = property("vanillin_version") as String
                groupId = property("vanillin_group") as String
            }
        }
    }
}

dependencies {
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")

    modCompileOnly("maven.modrinth:sodium:${property("sodium_version")}-fabric")
    modCompileOnly("maven.modrinth:iris:${property("iris_version")}-fabric")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
