package me.xtrm.unlok

import me.xtrm.unlok.delegate.FieldDelegate
import me.xtrm.unlok.delegate.MethodDelegate
import me.xtrm.unlok.util.supportsCurrentVM

/**
 * Unlocking the JVM.
 *
 * @author xtrm-en
 * @since 0.0.1
 */
object Unlok {
    init {
        if (!supportsCurrentVM()) {
            throw RuntimeException("Current JVM is not supported by Unlok.")
        }
    }

    /**
     * creates a [FieldDelegate] with the given arguments
     *
     * @param ownerClassName
     * @param fieldName
     * @param ownerInstance
     *
     * @return the newly created [FieldDelegate]
     */
    @JvmStatic
    fun <T> field(
        ownerClassName: String,
        fieldName: String = "",
        ownerInstance: Any? = null,
    ): FieldDelegate<T> =
        FieldDelegate(cleanClassName(ownerClassName), fieldName, ownerInstance)

    /**
     * creates a [MethodDelegate] with the given arguments
     *
     * @param ownerClassName
     * @param methodName
     * @param methodDesc
     * @param ownerInstance
     *
     * @return the newly created [MethodDelegate]
     */
    @JvmStatic
    fun <T> method(
        ownerClassName: String,
        methodName: String = "",
        methodDesc: String = "",
        ownerInstance: Any? = null,
    ): MethodDelegate<T> =
        MethodDelegate(
            cleanClassName(ownerClassName),
            methodName,
            methodDesc,
            ownerInstance
        )

    /**
     * cleans the given class name up, removing the 'L' at the start and the
     * ';' at the end if they are present.
     *
     * @param className the original class name
     *
     * @return the cleaned up class name
     */
    private fun cleanClassName(className: String): String =
        if (className.startsWith('L') && className.endsWith(';')) {
            className.substring(1, className.length - 1)
        } else className
}
