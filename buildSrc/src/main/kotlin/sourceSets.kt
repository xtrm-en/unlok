
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer

var apiExists = false

private fun Project.sourceSets(action: SourceSetContainer.() -> Unit) =
    (this as ExtensionAware).extensions.configure("sourceSets", action)

fun Project.addApiSourceSet() {
    sourceSets {
        val name = "api"

        val main = getByName("main")
        val test = getByName("test")

        val sourceSet = create(name) {
            java {
                srcDir("src/$name/kotlin")
                resources.srcDir("src/$name/resources")
            }

            this.compileClasspath += main.compileClasspath
            this.runtimeClasspath += main.runtimeClasspath
        }

        listOf(main, test).forEach {
            it.compileClasspath += sourceSet.output
            it.runtimeClasspath += sourceSet.output
        }

        apiExists = true
    }
}
