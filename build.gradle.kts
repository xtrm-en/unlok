import java.net.URL

plugins {
    `java-library`
    kotlin("jvm") version Plugins.KOTLIN
    id("org.jetbrains.dokka") version Plugins.DOKKA
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version Plugins.NEXUS_PUBLISH
    id("org.jlleitschuh.gradle.ktlint") version Plugins.KTLINT
}

val jvmTarget = "1.8"
val apiSourceSet = true

group = Coordinates.GROUP
version = Coordinates.VERSION

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.hackery.site")
    maven("https://jitpack.io")
}

dependencies {
    Dependencies.kotlinModules.forEach {
        implementation("org.jetbrains.kotlin", "kotlin-$it", Dependencies.KOTLIN)
    }

    implementation("com.github.stardust-enterprises", "unsafe", "967938cbdc")
    implementation("com.github.xtrm-en", "deencapsulation", "42b829f373")

    listOf("asm", "asm-tree").forEach {
        implementation("org.ow2.asm", it, Dependencies.ASM)
    }
    implementation("codes.som.anthony", "koffee", Dependencies.KOFFEE)

    testImplementation("org.jetbrains.kotlin", "kotlin-test", Dependencies.KOTLIN)
}

if (apiSourceSet) {
    sourceSets {
        val main by getting
        val test by getting

        val api by creating {
            java.srcDir("src/api/kotlin")
            resources.srcDir("src/api/resources")

            this.compileClasspath += main.compileClasspath
            this.runtimeClasspath += main.runtimeClasspath
        }

        listOf(main, test).forEach {
            it.compileClasspath += api.output
            it.runtimeClasspath += api.output
        }
    }
}

tasks {
    // Use JUnit as a testing framework
    test {
        useJUnitPlatform()
    }

    // Set the Java compilation target version
    compileKotlin {
        kotlinOptions.jvmTarget = jvmTarget
    }
    compileJava {
        targetCompatibility = jvmTarget
        sourceCompatibility = jvmTarget
    }

    // Configure dokka's renderer
    dokkaHtml {
        val moduleFile = File(projectDir, "MODULE.temp.MD")

        run {
            // In order to have a description on the rendered docs, we have to have
            // a file with the # Module thingy in it. That's what we're
            // automagically creating here.

            doFirst {
                moduleFile.writeText("# Module ${Coordinates.NAME}\n${Coordinates.DESC}")
            }

            doLast {
                moduleFile.delete()
            }
        }

        moduleName.set(Coordinates.NAME)

        dokkaSourceSets.configureEach {
            displayName.set(Coordinates.NAME)
            includes.from(moduleFile.path)

            skipDeprecated.set(false)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            reportUndocumented.set(true)

            sourceRoots.from(file("src/api/kotlin"))

            // Link the source to the documentation
            sourceLink {
                localDirectory.set(file("src"))
                remoteUrl.set(URL("https://github.com/${Coordinates.REPO_ID}/tree/trunk/src"))
            }
        }
    }

    // The original artifact, we just have to add the API source output and the
    // LICENSE file.
    jar {
        if (apiSourceSet) {
            from(sourceSets["api"].output)
        }
        from("LICENSE")
    }

    if (apiSourceSet) {
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
        if (apiSourceSet) {
            from(sourceSets["api"].allSource)
        }

        from("LICENSE")
    }

    // The Javadoc artifact, containing the Dokka output and the LICENSE file.
    create("javadocJar", Jar::class) {
        group = "build"

        archiveClassifier.set("javadoc")
        dependsOn(dokkaHtml)
        from(dokkaHtml)

        from("LICENSE")
    }
}

// List all artifact tasks
val artifactTasks = arrayOf(
    tasks["apiJar"],
    tasks["sourcesJar"],
    tasks["javadocJar"]
)

// Add our artifacts to the build process
artifacts {
    artifactTasks.forEach(::archives)
}

// Disable unwanted linting rules
ktlint {
    this.disabledRules.apply {
        add("no-wildcard-imports")
    }
}

// Sets up the Maven publication.
publishing.publications {
    create<MavenPublication>("mavenJava") {
        from(components["java"])
        artifactTasks.forEach(::artifact)

        // Infos from Coordinates.kt
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
    }
}

nexusPublishing.repositories.sonatype {
    nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
    snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

    // Skip this step if environment variables NEXUS_USERNAME or NEXUS_PASSWORD aren't set.
    username.set(properties["NEXUS_USERNAME"] as? String ?: return@sonatype)
    password.set(properties["NEXUS_PASSWORD"] as? String ?: return@sonatype)
}
