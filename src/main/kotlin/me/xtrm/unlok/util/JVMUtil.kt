package me.xtrm.unlok.util

/**
 * The magic accessor class, being either sun.reflect.MagicAccessorImpl or
 * jdk.internal.reflect.MagicAccessorImpl depending on the JVM support. If none
 * of these classes were found, the [supportsCurrentVM] method will return
 * false.
 */
internal val magicAccessorClass: Class<*>? by lazy {
    try {
        Class.forName("sun.reflect.MagicAccessorImpl")
    } catch (ex: ClassNotFoundException) {
        try {
            Class.forName("jdk.internal.reflect.MagicAccessorImpl")
        } catch (ignored: ClassNotFoundException) {
            null
        }
    }
}

/**
 * @return Whether Unlok supports the current VM.
 * @see magicAccessorClass
 */
internal fun supportsCurrentVM(): Boolean =
    magicAccessorClass != null
