
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

val Project.publishing: PublishingExtension get() =
    (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension

val Project.signing: SigningExtension get() =
    (this as ExtensionAware).extensions.getByName("signing") as SigningExtension

fun Project.setupMavenPublications() {
    publishing.publications {
        // Sets up the Maven integration.
        create("mavenJava", MavenPublication::class.java, Action {
            from(components["java"])
            defaultArtifactTasks.forEach(::artifact)

            pom {
                name.set(Coordinates.NAME)
                description.set(Coordinates.DESC)
                url.set("https://github.com/${Coordinates.REPO_ID}")

                licenses {
                    Pom.licenses.forEach {
                        license {
                            name.set(it.name)
                            url.set(it.url)
                            distribution.set(it.distribution)
                        }
                    }
                }

                developers {
                    Pom.developers.forEach {
                        developer {
                            id.set(it.id)
                            name.set(it.name)
                        }
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/${Coordinates.REPO_ID}.git")
                    developerConnection.set("scm:git:ssh://github.com/${Coordinates.REPO_ID}.git")
                    url.set("https://github.com/${Coordinates.REPO_ID}")
                }
            }

            // Configure the signing extension to sign this Maven artifact.
            signing.sign(this)
        })
    }
}
