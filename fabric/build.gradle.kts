plugins {
    idea
    java
    `maven-publish`
    id("dev.architectury.loom")
    id("flywheel.subproject")
    id("flywheel.platform")
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
        implementation(api, lib, backend)
    }

    createCompileConfigurations()
}

platform {
    commonProject = project(":common")
    sources(api, lib, backend, main)
    compileWithCommonSourceSets()
    setupLoomMod()
    setupLoomRuns()
    setupFatJar()
}

jarSets {
    mainSet.publish(platform.modArtifactId)
    create("api", api, lib).apply {
        addToAssemble()
        publish(platform.apiArtifactId)
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    modCompileOnly("maven.modrinth:sodium:${property("sodium_version")}")
    modCompileOnly("maven.modrinth:iris:${property("iris_version")}")

    "forApi"(project(path = ":common", configuration = "commonApiOnly"))
    "forLib"(project(path = ":common", configuration = "commonLib"))
    "forBackend"(project(path = ":common", configuration = "commonBackend"))
    "forMain"(project(path = ":common", configuration = "commonImpl"))
}
