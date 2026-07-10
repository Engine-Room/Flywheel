package dev.engine_room.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.internal.extensions.core.extra
import org.gradle.kotlin.dsl.getByType
import java.util.Optional

private val Project.libs: VersionCatalog
    get() = extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

private inline fun <T> VersionCatalog.find(
    alias: String,
    lookup: VersionCatalog.(String) -> Optional<T>
): T = lookup(alias).orElseThrow {
    IllegalArgumentException("$alias not found in version catalog")
}

fun Project.versionOf(alias: String) = libs.find(alias, VersionCatalog::findVersion).toString()
fun Project.libraryOf(alias: String) = libs.find(alias, VersionCatalog::findLibrary)

fun Project.getProperty(name: String) = providers.gradleProperty(name).get()

fun Project.getExt(name: String) = extra.get(name) as String
