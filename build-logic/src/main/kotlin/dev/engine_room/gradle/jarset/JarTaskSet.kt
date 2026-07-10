package dev.engine_room.gradle.jarset

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationPublications
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.tasks.JvmConstants
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.internal.JvmPluginServices
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.language.jvm.tasks.ProcessResources

class JarTaskSet(
    private val project: Project,
    private val name: String,
    val jar: TaskProvider<Jar>,
    val sources: TaskProvider<Jar>,
) {
    fun publish() {
        fun createConsumableConfiguration(
            configurationName: String,
            descriptionSuffix: String,
            outgoingArtifact: Any,
            configure: Configuration.() -> Unit,
            configureOutgoing: ConfigurationPublications.() -> Unit = {}
        ): Configuration {
            return project.configurations.consumable(name + configurationName.replaceFirstChar(Char::uppercaseChar)) {
                description = "$descriptionSuffix elements for '${this@JarTaskSet.name}' JarTaskSet"

                configure()

                outgoing {
                    capability("${project.group}:${project.name}-${this@JarTaskSet.name}:${project.version}")
                    artifact(outgoingArtifact)

                    configureOutgoing()
                }
            }.get()
        }

        val jvmPluginServices = project.serviceOf<JvmPluginServices>()
        val javaVersion = project.extensions
            .getByType<JavaPluginExtension>()
            .toolchain
            .languageVersion
            .get()
            .asInt()

        val apiElements = createConsumableConfiguration(
            configurationName = JvmConstants.API_ELEMENTS_CONFIGURATION_NAME,
            descriptionSuffix = "API",
            outgoingArtifact = jar,
            configure = {
                jvmPluginServices.configureAsApiElements(this)
                attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, javaVersion)
            },
            configureOutgoing = {
                attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            }
        )

        val sourcesElements = createConsumableConfiguration(
            configurationName = JvmConstants.SOURCES_ELEMENTS_CONFIGURATION_NAME,
            descriptionSuffix = "Sources",
            outgoingArtifact = sources,
            configure = {
                attributes {
                    attribute(Category.CATEGORY_ATTRIBUTE, named(Category.DOCUMENTATION))
                    attribute(Bundling.BUNDLING_ATTRIBUTE, named(Bundling.EXTERNAL))
                    attribute(DocsType.DOCS_TYPE_ATTRIBUTE, named(DocsType.SOURCES))
                    attribute(Usage.USAGE_ATTRIBUTE, named(Usage.JAVA_RUNTIME))
                }
            },
        )

        val java = project.components.getByName<AdhocComponentWithVariants>("java")
        java.addVariantsFromConfiguration(apiElements) {}
        java.addVariantsFromConfiguration(sourcesElements) {}
    }

    fun outgoing(name: String) {
        outgoingJar(name)
    }

    fun outgoingJar(name: String) {
        val config = project.configurations.register(name) {
            isCanBeConsumed = true
            isCanBeResolved = false
        }

        project.artifacts.add(config.name, jar)
    }

    /**
     * Configure the assemble task to depend on the regular jar tasks and javadoc jar.
     */
    fun addToAssemble() {
        project.tasks.named("assemble").configure {
            dependsOn(jar, sources)
        }
    }

    /**
     * Configure the jar tasks with the given action.
     */
    fun configureJar(action: Action<Jar>) {
        jar.configure(action)
        sources.configure(action)
    }

    companion object {
        private const val PACKAGE_INFOS_JAVA_PATTERN = "**/package-info.java"
        private const val BUILD_GROUP: String = "build"
        private const val SOURCES_CLASSIFIER: String = "sources"

        /**
         * We have duplicate packages between the common and platform dependent subprojects.
         * In theory the package-info.java files should be identical, so just take the first one we find.
         */
        fun excludeDuplicatePackageInfos(copyTask: AbstractCopyTask) {
            copyTask.filesMatching(PACKAGE_INFOS_JAVA_PATTERN) {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }

        /**
         * The compile/javadoc tasks have a different base type that isn't so smart about exclusion handling.
         */
        fun excludeDuplicatePackageInfos(sourceTask: SourceTask) {
            sourceTask.exclude(PACKAGE_INFOS_JAVA_PATTERN)
        }

        fun create(project: Project, name: String, vararg sourceSetSet: SourceSet): JarTaskSet {
            val jarTask = createJar(project, name, sourceSetSet)
            val sourcesTask = createSourcesJar(project, name, sourceSetSet)

            return JarTaskSet(project, name, jarTask, sourcesTask)
        }

        private fun createJar(
            project: Project,
            name: String,
            sourceSetSet: Array<out SourceSet>
        ): TaskProvider<Jar> {
            return project.tasks.register<Jar>("${name}Jar") {
                group = BUILD_GROUP
                archiveClassifier.set(name)

                for (set in sourceSetSet) {
                    from(set.output)
                }
                excludeDuplicatePackageInfos(this)
            }
        }

        private fun createSourcesJar(
            project: Project,
            name: String,
            sourceSetSet: Array<out SourceSet>
        ): TaskProvider<Jar> {
            return project.tasks.register<Jar>("${name}SourcesJar") {
                group = BUILD_GROUP
                archiveClassifier.set("$name-$SOURCES_CLASSIFIER")

                for (set in sourceSetSet) {
                    // In the platform projects we inject common sources into these tasks to avoid polluting the
                    // source set itself, but that bites us here, and we need to roll in all the sources in this kinda
                    // ugly way. There's probably a better way to do this but JarTaskSet can't easily accommodate it.
                    from(project.tasks.named<ProcessResources>(set.processResourcesTaskName))
                    from(project.tasks.named<JavaCompile>(set.compileJavaTaskName).map { it.source.asFileTree })
                }
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            }
        }
    }
}
