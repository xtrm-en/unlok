plugins {
    kotlin("jvm") version "1.6.10"
}

group = "me.xtrm"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.hackery.site")
}

dependencies {
    listOf("stdlib", "reflect").forEach {
        implementation("org.jetbrains.kotlin", "kotlin-$it", "1.6.10")
    }
    listOf("asm", "asm-tree").forEach {
        implementation("org.ow2.asm", it, "9.2")
    }
    implementation("codes.som.anthony", "koffee", "8.0.2")

    testImplementation("org.jetbrains.kotlin", "kotlin-test", "1.6.10")
}

tasks.test {
    useJUnitPlatform()
}