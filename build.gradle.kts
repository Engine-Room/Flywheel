plugins {
    idea
    java
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.mdg) apply false
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

idea {
    // Tell IDEA to always download sources/javadoc artifacts from maven.
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
