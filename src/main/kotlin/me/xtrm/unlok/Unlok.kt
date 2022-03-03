package me.xtrm.unlok

import me.xtrm.unlok.delegate.FieldDelegate
import me.xtrm.unlok.utils.supportsCurrentVM

/**
 * Unlocking the JVM.
 *
 * @author xtrm-en
 * @since 0.0.1
 */
object Unlok {

    init {
        if (!supportsCurrentVM()) {
            throw RuntimeException("Current JVM isn't suppported by Unlok.")
        }
    }

    @JvmStatic
    fun <T> field(
        ownerClass: String,
        fieldName: String = "",
        ownerInstance: Any? = null,
    ): FieldDelegate<T> {
        var className = ownerClass.replace('.', '/')
        if (className.startsWith('L') && className.endsWith(";")) {
            className = className.substring(1, className.length - 1)
        }

        return FieldDelegate(className, fieldName, ownerInstance)
    }

    @JvmStatic
    fun <T> method(
        ownerClass: String,
        methodName: String = "",
        ownerInstance: Any? = null,
    ): FieldDelegate<T> {
        var className = ownerClass.replace('.', '/')
        if (className.startsWith('L') && className.endsWith(";")) {
            className = className.substring(1, className.length - 1)
        }

        return MethodDelegate<T>(className, methodName, ownerInstance)
    }
}
