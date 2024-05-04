plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.package-infos")
    id("flywheel.subproject")
    id("flywheel.jar-sets")
    id("flywheel.transitive-source-sets")
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

// For sharing with other subprojects.
jarSets {
    createJars("apiOnly", api).createOutgoingConfiguration("common")
    createJars("lib").createOutgoingConfiguration("common")
    createJars("backend").createOutgoingConfiguration("common")
    createJars("impl", main).createOutgoingConfiguration("common")
}

// For publishing
val apiLibJar = jarSets.createJars("api", api, lib)

dependencies {
    modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        register<MavenPublication>("mavenIntermediary") {
            artifact(apiLibJar.remapJar)
            artifact(apiLibJar.remapSources)
            artifact(apiLibJar.javadocJar)
            artifactId = "flywheel-common-intermediary-api-${property("artifact_minecraft_version")}"
        }
        register<MavenPublication>("mavenMojmap") {
            artifact(apiLibJar.jar)
            artifact(apiLibJar.sources)
            artifact(apiLibJar.javadocJar)
            artifactId = "flywheel-common-mojmap-api-${property("artifact_minecraft_version")}"
        }
    }
}
