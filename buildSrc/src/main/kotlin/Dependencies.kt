private const val kotlinVersion = "1.6.10"

object Plugins {
    const val KOTLIN = kotlinVersion
    const val GRGIT = "4.1.1" // old version for jgit to work on Java 8
    const val BLOSSOM = "1.3.0"
    const val SHADOW = "7.1.2"
    const val KTLINT = "10.2.1"
    const val DOKKA = kotlinVersion
    const val NEXUS_PUBLISH = "1.0.0"
}

object Dependencies {
    const val KOTLIN = kotlinVersion
    const val ASM = "9.2"
    const val KOFFEE = "8.0.2"
    const val DEENCAPSULATION = "42b829f373"
    const val UNSAFE = "1.7.1"

    val kotlinModules = arrayOf("stdlib", "reflect")
}

object Repositories {
    val mavenUrls = arrayOf(
        "https://maven.hackery.site",
        "https://jitpack.io/",
    )
}
