object Coordinates {
    const val NAME = "unlok"
    const val DESC = "Unlock your JVM, access any field or method without Reflection."
    const val REPO_ID = "stardust-enterprises/$NAME"

    const val GROUP = "me.xtrm"
    const val VERSION = "0.1.0"
}

object Pom {
    val licenses = arrayOf(
        License("ISC License", "https://opensource.org/licenses/ISC")
    )
    val developers = arrayOf(
        Developer("xtrm")
    )
}

data class License(val name: String, val url: String, val distribution: String = "repo")
data class Developer(val id: String, val name: String = id)
