plugins {
    idea
    java
    id("dev.architectury.loom") apply false
    id("dev.architectury.loom-no-remap") apply false
}

allprojects {
    tasks.withType<org.gradle.api.tasks.AbstractCopyTask>().configureEach {
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    }
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

idea {
    // Tell IDEA to always download sources/javadoc artifacts from maven.
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
