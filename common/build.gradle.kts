plugins {
    alias(libs.plugins.flywheel.gradle)
    alias(libs.plugins.configure.base)
    alias(libs.plugins.configure.common)
    alias(libs.plugins.setup.repositories)
}

val api = sourceSets.create("api")
val lib = sourceSets.create("lib")
val backend = sourceSets.create("backend")
val main = sourceSets.getByName("main")

loom.accessWidenerPath = file("src/main/resources/flywheel.accesswidener")

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
    sourceSet(main) {
        compileClasspath(api, lib, backend)
        outgoing()
    }
    sourceSet(sourceSets.getByName("test")) {
        implementation(api, lib, backend)
    }
}

defaultPackageInfos {
    sources(api, lib, backend, main)
}

jarSets {
    create("api", api, lib).apply {
        addToAssemble()
        publish()
    }
}

dependencies {
    compileOnly(libs.bundles.mixin)
    annotationProcessor(libs.mixin.extras)

    compileOnly(libs.sodium.fabric.api)
    compileOnly(libs.iris.fabric)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
}
