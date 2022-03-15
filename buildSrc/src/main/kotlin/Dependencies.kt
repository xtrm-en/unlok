private const val kotlinVersion = "1.6.10"

object Plugins {
    const val KOTLIN = kotlinVersion
    const val DOKKA = kotlinVersion
    const val NEXUS_PUBLISH = "1.0.0"
    const val KTLINT = "10.2.1"
}

object Dependencies {
    const val KOTLIN = kotlinVersion
    const val ASM = "9.2"
    const val KOFFEE = "8.0.2"

    val kotlinModules = arrayOf("stdlib", "reflect")
}
