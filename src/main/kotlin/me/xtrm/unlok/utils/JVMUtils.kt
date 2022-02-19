package me.xtrm.unlok.utils

private var initialized = false
var magicAccessorClass: Class<*>? = null
    private set

fun supportsCurrentVM(): Boolean {
    if(!initialized) {
        init()
    }
    return magicAccessorClass != null
}

private fun init() {
    initialized = true

    magicAccessorClass = try {
        Class.forName("sun.reflect.MagicAccessorImpl")
    } catch (exception: ClassNotFoundException) {
        try {
            Class.forName("jdk.internal.reflect.MagicAccessorImpl")
        } catch(exception2: ClassNotFoundException) {
            throw exception
        }
    }
}
