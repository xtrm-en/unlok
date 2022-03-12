package me.xtrm.unlok

import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.delegate.FieldDelegate
import me.xtrm.unlok.delegate.MethodDelegate
import me.xtrm.unlok.util.supportsCurrentVM

/**
 * Unlocking the JVM.
 *
 * @author xtrm, lambdagg
 * @since 0.0.1
 */
object Unlok {
    init {
        if (!supportsCurrentVM()) {
            throw RuntimeException("Current JVM is not supported by Unlok.")
        }
    }

    /**
     * Provides a new [FieldDelegate] corresponding to given arguments.
     *
     * @param ownerClassName The name of the class that holds the wanted field.
     * @param fieldName The name of the wanted field.
     * @param ownerInstance If the wanted field is static, this parameter *has*
     *                      to be empty. Otherwise, we need a value to get the
     *                      field value from.
     *
     * @return The newly created [FieldAccessor].
     */
    @JvmStatic
    fun <T> field(
        ownerClassName: String,
        fieldName: String = "",
        ownerInstance: Any? = null,
    ): FieldDelegate<T> =
        FieldDelegate(
            cleanClassName(ownerClassName),
            fieldName,
            ownerInstance
        )

    /**
     * Provides a new [MethodDelegate] corresponding to given arguments.
     *
     * @param ownerClassName The name of the class that holds the wanted
     *                       method.
     * @param methodName The name of the wanted method.
     * @param methodDesc The description of the wanted method, if needed.
     * @param ownerInstance If the wanted field is static, this parameter *has*
     *                      to be empty. Otherwise, we need a value to get the
     *                      field value from.
     *
     * @return The newly created [MethodDelegate].
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
     * Cleans the given class name up, removing the 'L' at the start and the
     * ';' at the end if they are present.
     *
     * @param className The original class name.
     *
     * @return The cleaned up class name.
     */
    private fun cleanClassName(className: String): String =
        if (className.startsWith('L') && className.endsWith(';')) {
            className.substring(1, className.length - 1)
        } else className
}
