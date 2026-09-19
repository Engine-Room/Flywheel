repositories {
    mavenCentral()
    maven("https://maven.caffeinemc.net/releases") {
        content {
            includeModuleByRegex("net.caffeinemc", "sodium-(?:fabric|neoforge)-api")
        }
    }
    maven("https://api.modrinth.com/maven") {
        content {
            includeModule("maven.modrinth", "iris")
        }
    }
}
