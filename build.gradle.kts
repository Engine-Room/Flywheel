plugins {
    id("idea")
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

// Tell IDEA to always download sources/javadoc artifacts from maven.
idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}
