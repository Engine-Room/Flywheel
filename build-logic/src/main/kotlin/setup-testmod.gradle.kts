import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

val sourceSets = extensions.getByType(SourceSetContainer::class)
val testModSourceSet = sourceSets.maybeCreate("testMod")

val testModJar = tasks.register<Jar>("testModJar") {
    from(testModSourceSet.output)
    archiveClassifier = "testmod"
}

tasks.named<Task>("build").configure {
    dependsOn(testModJar)
}
