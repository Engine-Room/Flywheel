package dev.engine_room.gradle

object Constants {
    val JVM_ARGUMENTS = listOf(
        // Better hotswap, only available with the JetBrains Runtime so we have to ignore invalid options aswell
        "-XX:+IgnoreUnrecognizedVMOptions",
        "-XX:+AllowEnhancedClassRedefinition"
    )

    val SYSTEM_PROPERTIES = mapOf(
        // Turn on our own debug flags
        "flw.devEnv" to "true",
        "flw.dumpShaderSource" to "true",
        "flw.debugMemorySafety" to "true",

        // Turn on mixin debug flags
        "mixin.debug.export" to "true",
        "mixin.debug.verbose" to "true"
    )

    val PROGRAM_ARGUMENTS = listOf(
        // 720p baby!
        "--width", "1280", "--height", "720",

        // Helpful when debugging
        "--renderDebugLabels"
    )
}
