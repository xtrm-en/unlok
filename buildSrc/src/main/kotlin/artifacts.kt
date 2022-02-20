
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

private val Project.sourceSets: SourceSetContainer
    get() =
        (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

val defaultArtifactTasks = mutableListOf<Task>()

fun Project.addDefaultArtifacts() {
    val block: TaskContainer.() -> Unit = {
        // The original artifact, we just have to add the API source output and the
        // LICENSE file.
        named<org.gradle.api.tasks.bundling.Jar>("jar") {
            if (apiExists) {
                from(sourceSets["api"].output)
            }
            from("LICENSE")
        }

        if (apiExists) {
            // API artifact, only including the output of the API source and the
            // LICENSE file.
            create("apiJar", Jar::class) {
                group = "build"

                archiveClassifier.set("api")
                from(sourceSets["api"].output)

                from("LICENSE")
            }
        }

        // Source artifact, including everything the 'main' does but not compiled.
        create("sourcesJar", Jar::class) {
            group = "build"

            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
            if (apiExists) {
                from(sourceSets["api"].allSource)
            }

            from("LICENSE")
        }

        val dokkaHtml = getByName("dokkaHtml")

        // The Javadoc artifact, containing the Dokka output and the LICENSE file.
        create("javadocJar", Jar::class) {
            group = "build"

            archiveClassifier.set("javadoc")
            dependsOn(dokkaHtml)
            from(dokkaHtml)

            from("LICENSE")
        }
    }
    this.tasks.block()

    artifacts {
        defaultArtifactTasks.also { it.plusAssign(arrayOf(
            tasks["sourcesJar"],
            tasks["javadocJar"]
        ).apply { if (apiExists) this.plus(tasks["apiJar"]) }) }
            .forEach { add("archives", it) }
    }
}
