package me.xtrm.unlok.util

private var initialized = false

var magicAccessorClass: Class<*>? = null
    private set

fun supportsCurrentVM(): Boolean {
    return init() != null
}

private fun init(): Class<*>? {
    if (!initialized) {
        initialized = true

        magicAccessorClass = try {
            Class.forName("sun.reflect.MagicAccessorImpl")
        } catch (ex: ClassNotFoundException) {
            try {
                Class.forName("jdk.internal.reflect.MagicAccessorImpl")
            } catch (ignored: ClassNotFoundException) {
                throw ex
            }
        }
    }

    return magicAccessorClass
}
