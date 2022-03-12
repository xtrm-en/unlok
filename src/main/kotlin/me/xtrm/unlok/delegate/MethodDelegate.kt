package me.xtrm.unlok.delegate

import me.xtrm.unlok.accessor.AccessorBuilder
import me.xtrm.unlok.api.accessor.MethodAccessor
import kotlin.reflect.KProperty

/**
 * @author xtrm
 * @since 0.0.1
 */
class MethodDelegate<T> (
    private val ownerClass: String,
    private val methodName: String = "",
    private val methodDesc: String = "",
    private val owner: Any? = null,
) {
    private var accessor: MethodAccessor<T>? = null

    init {
        if (this.methodName.isNotBlank()) {
            this.accessor = AccessorBuilder.methodAccessor(
                this.ownerClass,
                this.methodName,
                this.methodDesc,
                this.owner
            )
        }
    }

    /**
     * @param arguments the method args
     *
     * @return the method's return value
     */
    operator fun invoke(vararg arguments: Any?): T? =
        this.accessor?.invoke(arguments)

    /**
     * @return this instance
     */
    operator fun getValue(t: Any?, property: KProperty<*>): MethodDelegate<T> =
        this.apply {
            if (this.accessor == null) {
                this.accessor = AccessorBuilder.methodAccessor(
                    this.ownerClass,
                    this.methodName.ifBlank { property.name },
                    this.methodDesc,
                    this.owner
                )
            }
        }
}
